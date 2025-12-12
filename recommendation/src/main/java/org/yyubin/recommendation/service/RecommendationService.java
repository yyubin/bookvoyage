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
     * @param userId 사용자 ID
     * @param limit 추천할 도서 수
     * @param forceRefresh 캐시 무시하고 재계산 여부
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> generateRecommendations(Long userId, int limit, boolean forceRefresh) {
        log.info("Generating recommendations for user {} (limit: {}, forceRefresh: {})",
                userId, limit, forceRefresh);

        // 1. 캐시 확인
        if (!forceRefresh && cacheService.hasCachedRecommendations(userId)) {
            log.debug("Using cached recommendations for user {}", userId);
            return cacheService.getRecommendations(userId, limit);
        }

        // 2. 후보 생성
        List<RecommendationCandidate> candidates = generateCandidates(userId);

        if (candidates.isEmpty()) {
            log.warn("No candidates generated for user {}", userId);
            return List.of();
        }

        // 3. 중복 제거 (같은 bookId는 점수가 높은 것만 유지)
        Map<Long, RecommendationCandidate> uniqueCandidates = candidates.stream()
                .collect(Collectors.toMap(
                        RecommendationCandidate::getBookId,
                        c -> c,
                        (c1, c2) -> c1.getInitialScore() > c2.getInitialScore() ? c1 : c2
                ));

        log.debug("Unique candidates: {} (from {} total)", uniqueCandidates.size(), candidates.size());

        // 4. 스코어링
        Map<Long, Double> scores = hybridScorer.batchCalculate(userId, new ArrayList<>(uniqueCandidates.values()));

        // 5. Redis에 저장
        cacheService.saveRecommendations(userId, scores);

        // 6. 정렬 및 결과 반환
        List<RecommendationResult> results = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
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

        // 7. 순위 매기기
        int rank = 1;
        for (RecommendationResult result : results) {
            result.setRank(rank++);
        }

        log.info("Generated {} recommendations for user {}", results.size(), userId);
        return results;
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

    @lombok.Data
    @lombok.Builder
    public static class RecommendationStats {
        private Long userId;
        private long cachedItems;
        private long cacheTtlSeconds;
        private boolean hasCachedRecommendations;
    }
}
