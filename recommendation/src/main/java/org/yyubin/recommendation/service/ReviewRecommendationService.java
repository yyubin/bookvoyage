package org.yyubin.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yyubin.recommendation.candidate.ReviewElasticsearchCandidateGenerator;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.scoring.review.ReviewHybridScorer;

/**
 * 리뷰 추천 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRecommendationService {

    private final ReviewElasticsearchCandidateGenerator elasticsearchCandidateGenerator;
    private final ReviewHybridScorer hybridScorer;
    private final ReviewRecommendationCacheService cacheService;
    private final ReviewRecommendationExposureService exposureService;
    private final ReviewRecommendationProperties properties;

    /**
     * 피드용 리뷰 추천
     */
    public List<ReviewRecommendationResult> recommendFeed(Long userId, int limit, boolean forceRefresh) {
        return generate(userId, null, null, limit, forceRefresh);
    }

    /**
     * 피드용 리뷰 추천 (cursor 기반 페이징)
     */
    public List<ReviewRecommendationResult> recommendFeed(Long userId, Long cursor, int limit, boolean forceRefresh) {
        return generate(userId, null, cursor, limit, forceRefresh);
    }

    /**
     * 특정 도서 컨텍스트의 리뷰 추천
     */
    public List<ReviewRecommendationResult> recommendForBook(Long userId, Long bookId, int limit, boolean forceRefresh) {
        return generate(userId, bookId, null, limit, forceRefresh);
    }

    /**
     * 특정 도서 컨텍스트의 리뷰 추천 (cursor 기반 페이징)
     */
    public List<ReviewRecommendationResult> recommendForBook(Long userId, Long bookId, Long cursor, int limit, boolean forceRefresh) {
        return generate(userId, bookId, cursor, limit, forceRefresh);
    }

    private List<ReviewRecommendationResult> generate(Long userId, Long bookContextId, Long cursor, int limit, boolean forceRefresh) {
        if (!forceRefresh && cacheService.exists(userId, bookContextId)) {
            List<ReviewRecommendationResult> cached = cacheService.get(userId, bookContextId, cursor, limit);
            if (bookContextId == null && userId != null) {
                exposureService.recordExposure(userId, cached.stream()
                        .map(ReviewRecommendationResult::getReviewId)
                        .toList());
            }
            return cached;
        }

        List<ReviewRecommendationCandidate> candidates = generateCandidates(userId, bookContextId);
        if (bookContextId == null && userId != null) {
            candidates = filterRecentlyExposed(userId, candidates);
        }
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, ReviewRecommendationCandidate> unique = candidates.stream()
                .filter(c -> c.getReviewId() != null)
                .collect(Collectors.toMap(
                        ReviewRecommendationCandidate::getReviewId,
                        c -> c,
                        (c1, c2) -> {
                            double s1 = c1.getInitialScore() != null ? c1.getInitialScore() : 0.0;
                            double s2 = c2.getInitialScore() != null ? c2.getInitialScore() : 0.0;
                            return s1 >= s2 ? c1 : c2;
                        }
                ));

        Map<Long, Double> scores = hybridScorer.batchCalculate(userId, bookContextId, new ArrayList<>(unique.values()));
        cacheService.save(userId, bookContextId, scores);

        List<ReviewRecommendationResult> allResults = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(entry -> {
                    ReviewRecommendationCandidate candidate = unique.get(entry.getKey());
                    return ReviewRecommendationResult.builder()
                            .reviewId(entry.getKey())
                            .bookId(candidate != null ? candidate.getBookId() : null)
                            .score(entry.getValue())
                            .source(candidate != null && candidate.getSource() != null ? candidate.getSource().name() : null)
                            .reason(candidate != null ? candidate.getReason() : null)
                            .createdAt(candidate != null ? candidate.getCreatedAt() : null)
                            .build();
                })
                .toList();

        // cursor 기반 페이징
        List<ReviewRecommendationResult> results = applyCursorPagination(allResults, cursor, limit);

        int rank = 1;
        for (ReviewRecommendationResult result : results) {
            result.setRank(rank++);
        }

        if (bookContextId == null && userId != null) {
            exposureService.recordExposure(userId, results.stream()
                    .map(ReviewRecommendationResult::getReviewId)
                    .toList());
        }

        log.info("Generated {} review recommendations for user {} (contextBook={})", results.size(), userId, bookContextId);
        return results;
    }

    private List<ReviewRecommendationCandidate> generateCandidates(Long userId, Long bookContextId) {
        int maxCandidates = properties.getMaxCandidates();
        if (bookContextId != null) {
            return elasticsearchCandidateGenerator.generateBookScopedCandidates(bookContextId, maxCandidates);
        }
        return elasticsearchCandidateGenerator.generateFeedCandidates(userId, maxCandidates);
    }

    private List<ReviewRecommendationCandidate> filterRecentlyExposed(
            Long userId,
            List<ReviewRecommendationCandidate> candidates
    ) {
        var exposed = exposureService.loadRecentReviewIds(userId);
        if (exposed.isEmpty()) {
            return candidates;
        }
        List<ReviewRecommendationCandidate> filtered = candidates.stream()
                .filter(candidate -> candidate.getReviewId() != null)
                .filter(candidate -> !exposed.contains(candidate.getReviewId()))
                .toList();
        return filtered.isEmpty() ? candidates : filtered;
    }

    private List<ReviewRecommendationResult> applyCursorPagination(
            List<ReviewRecommendationResult> allResults,
            Long cursor,
            int limit
    ) {
        if (cursor == null) {
            return allResults.stream().limit(limit).toList();
        }

        // cursor 이후의 항목들만 필터링
        boolean foundCursor = false;
        List<ReviewRecommendationResult> results = new ArrayList<>();
        for (ReviewRecommendationResult result : allResults) {
            if (foundCursor) {
                results.add(result);
                if (results.size() >= limit) {
                    break;
                }
            } else if (result.getReviewId().equals(cursor)) {
                foundCursor = true;
            }
        }
        return results;
    }
}
