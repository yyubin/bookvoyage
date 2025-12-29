package org.yyubin.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.recommendation.candidate.ElasticsearchCandidateGenerator;
import org.yyubin.recommendation.candidate.Neo4jCandidateGenerator;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.scoring.HybridScorer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 추천 서비스
 * - 후보 생성, 스코어링, 캐싱을 통합하는 메인 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final Neo4jCandidateGenerator neo4jCandidateGenerator;
    private final ElasticsearchCandidateGenerator elasticsearchCandidateGenerator;
    private final HybridScorer hybridScorer;
    private final RecommendationCacheService cacheService;
    private final RecommendationProperties properties;

    /**
     * 사용자 맞춤 추천 생성
     *
     * @param userId 사용자 ID (nullable - null이면 비로그인 사용자)
     * @param limit 추천할 도서 수
     * @param forceRefresh 캐시 무시하고 재계산 여부
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> generateRecommendations(Long userId, int limit, boolean forceRefresh) {
        return generateRecommendations(userId, null, limit, forceRefresh);
    }

    /**
     * 사용자 맞춤 추천 생성 (cursor 기반 페이징)
     *
     * @param userId 사용자 ID (nullable - null이면 비로그인 사용자)
     * @param cursor 이전 페이지의 마지막 bookId
     * @param limit 추천할 도서 수
     * @param forceRefresh 캐시 무시하고 재계산 여부
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> generateRecommendations(Long userId, Long cursor, int limit, boolean forceRefresh) {
        log.info("Generating recommendations for user {} (cursor: {}, limit: {}, forceRefresh: {})",
                userId, cursor, limit, forceRefresh);

        // 비로그인 사용자는 기본 추천 반환
        if (userId == null) {
            log.info("Generating default recommendations for non-logged-in user");
            return generateDefaultRecommendations(cursor, limit);
        }

        // 1. 캐시 확인
        if (!forceRefresh && cacheService.hasCachedRecommendations(userId)) {
            log.debug("Using cached recommendations for user {}", userId);
            return cacheService.getRecommendations(userId, cursor, limit);
        }

        // 2. 후보 생성
        List<RecommendationCandidate> candidates = generateCandidates(userId);

        // 3. Fallback: 개인화 데이터가 없으면 인기 도서로 대체
        if (candidates.isEmpty()) {
            log.warn("No personalized candidates generated for user {}, falling back to popular books", userId);
            return generateDefaultRecommendations(limit);
        }

        // 4. 중복 제거 (같은 bookId는 점수가 높은 것만 유지)
        Map<Long, RecommendationCandidate> uniqueCandidates = candidates.stream()
                .collect(Collectors.toMap(
                        RecommendationCandidate::getBookId,
                        c -> c,
                        (c1, c2) -> c1.getInitialScore() > c2.getInitialScore() ? c1 : c2
                ));

        log.debug("Unique candidates: {} (from {} total)", uniqueCandidates.size(), candidates.size());

        // 5. 스코어링
        Map<Long, Double> scores = hybridScorer.batchCalculate(userId, new ArrayList<>(uniqueCandidates.values()));

        // 6. Redis에 저장
        cacheService.saveRecommendations(userId, scores);

        // 7. 정렬 및 결과 반환
        List<RecommendationResult> allResults = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(entry -> {
                    RecommendationCandidate candidate = uniqueCandidates.get(entry.getKey());
                    return RecommendationResult.builder()
                            .bookId(entry.getKey())
                            .score(entry.getValue())
                            .source(candidate.getSource().name())
                            .reason(candidate.getReason())
                            .build();
                })
                .toList();

        // 8. cursor 기반 페이징
        List<RecommendationResult> results = applyCursorPagination(allResults, cursor, limit);

        // 9. 순위 매기기
        int rank = 1;
        for (RecommendationResult result : results) {
            result.setRank(rank++);
        }

        log.info("Generated {} recommendations for user {}", results.size(), userId);
        return results;
    }

    /**
     * 기본 추천 생성 (비로그인 또는 초기 사용자용)
     * - 인기 도서 기반 추천 반환
     *
     * @param limit 추천할 도서 수
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> generateDefaultRecommendations(int limit) {
        return generateDefaultRecommendations(null, limit);
    }

    /**
     * 기본 추천 생성 (cursor 기반 페이징)
     *
     * @param cursor 이전 페이지의 마지막 bookId
     * @param limit 추천할 도서 수
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> generateDefaultRecommendations(Long cursor, int limit) {
        log.info("Generating default recommendations (cursor: {}, limit: {})", cursor, limit);

        try {
            // Elasticsearch에서 인기 도서 후보 생성 (cursor 고려하여 더 많이 가져옴)
            int fetchLimit = cursor != null ? limit * 3 : limit;
            List<RecommendationCandidate> popularCandidates =
                    elasticsearchCandidateGenerator.generateCandidates(null, fetchLimit);

            if (popularCandidates.isEmpty()) {
                log.warn("No popular books found for default recommendations");
                return List.of();
            }

            // 후보를 결과로 변환 (스코어링 없이 초기 점수 사용)
            List<RecommendationResult> allResults = popularCandidates.stream()
                    .map(candidate -> RecommendationResult.builder()
                            .bookId(candidate.getBookId())
                            .score(candidate.getInitialScore())
                            .source(candidate.getSource().name())
                            .reason(candidate.getReason())
                            .build())
                    .toList();

            // cursor 기반 페이징
            List<RecommendationResult> results = applyCursorPagination(allResults, cursor, limit);

            // 순위 매기기
            int rank = 1;
            for (RecommendationResult result : results) {
                result.setRank(rank++);
            }

            log.info("Generated {} default recommendations", results.size());
            return results;

        } catch (Exception e) {
            log.error("Failed to generate default recommendations", e);
            return List.of();
        }
    }

    /**
     * 캐시된 추천 조회
     *
     * @param userId 사용자 ID
     * @param limit 조회할 개수
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> getCachedRecommendations(Long userId, int limit) {
        return cacheService.getRecommendations(userId, limit);
    }

    /**
     * 캐시된 추천 조회 (cursor 기반 페이징)
     *
     * @param userId 사용자 ID
     * @param cursor 이전 페이지의 마지막 bookId
     * @param limit 조회할 개수
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> getCachedRecommendations(Long userId, Long cursor, int limit) {
        return cacheService.getRecommendations(userId, cursor, limit);
    }

    /**
     * 추천 스코어 상세 정보 (디버깅용)
     *
     * @param userId 사용자 ID
     * @param bookId 도서 ID
     * @return 스코어 상세 정보
     */
    public HybridScorer.ScoreBreakdown getScoreBreakdown(Long userId, Long bookId) {
        // 임시 후보 생성
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(bookId)
                .source(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE)
                .initialScore(0.5)
                .build();

        return hybridScorer.getScoreBreakdown(userId, candidate);
    }

    /**
     * 추천 캐시 새로고침
     *
     * @param userId 사용자 ID
     */
    public void refreshRecommendations(Long userId) {
        log.info("Refreshing recommendations for user {}", userId);
        cacheService.clearRecommendations(userId);
        generateRecommendations(userId, 50, true);
    }

    /**
     * 후보 생성 (내부 메서드)
     */
    private List<RecommendationCandidate> generateCandidates(Long userId) {
        List<RecommendationCandidate> candidates = new ArrayList<>();

        int maxCandidates = properties.getSearch().getMaxCandidates();
        int perGeneratorLimit = maxCandidates / 2; // Neo4j와 Elasticsearch에 균등 분배

        try {
            // Neo4j 기반 후보
            List<RecommendationCandidate> neo4jCandidates =
                    neo4jCandidateGenerator.generateCandidates(userId, perGeneratorLimit);
            candidates.addAll(neo4jCandidates);
            log.debug("Generated {} Neo4j candidates", neo4jCandidates.size());

        } catch (Exception e) {
            log.error("Failed to generate Neo4j candidates for user {}", userId, e);
        }

        try {
            // Elasticsearch 기반 후보
            List<RecommendationCandidate> esCandidates =
                    elasticsearchCandidateGenerator.generateCandidates(userId, perGeneratorLimit);
            candidates.addAll(esCandidates);
            log.debug("Generated {} Elasticsearch candidates", esCandidates.size());

        } catch (Exception e) {
            log.error("Failed to generate Elasticsearch candidates for user {}", userId, e);
        }

        return candidates;
    }

    /**
     * 통계 정보
     */
    public RecommendationStats getStats(Long userId) {
        var cacheStats = cacheService.getCacheStats(userId);

        return RecommendationStats.builder()
                .userId(userId)
                .cachedItems(cacheStats.getCachedItems())
                .cacheTtlSeconds(cacheStats.getTtlSeconds())
                .hasCachedRecommendations(cacheStats.isExists())
                .build();
    }

    private List<RecommendationResult> applyCursorPagination(
            List<RecommendationResult> allResults,
            Long cursor,
            int limit
    ) {
        if (cursor == null) {
            return allResults.stream().limit(limit).toList();
        }

        // cursor 이후의 항목들만 필터링
        boolean foundCursor = false;
        List<RecommendationResult> results = new ArrayList<>();
        for (RecommendationResult result : allResults) {
            if (foundCursor) {
                results.add(result);
                if (results.size() >= limit) {
                    break;
                }
            } else if (result.getBookId().equals(cursor)) {
                foundCursor = true;
            }
        }
        return results;
    }

    @lombok.Data
    @lombok.Builder
    public static class RecommendationStats {
        private Long userId;
        private long cachedItems;
        private long cacheTtlSeconds;
        private boolean hasCachedRecommendations;
    }
}
