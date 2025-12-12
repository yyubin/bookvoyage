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
    private final ReviewRecommendationProperties properties;

    /**
     * 피드용 리뷰 추천
     */
    public List<ReviewRecommendationResult> recommendFeed(Long userId, int limit, boolean forceRefresh) {
        return generate(userId, null, limit, forceRefresh);
    }

    /**
     * 특정 도서 컨텍스트의 리뷰 추천
     */
    public List<ReviewRecommendationResult> recommendForBook(Long userId, Long bookId, int limit, boolean forceRefresh) {
        return generate(userId, bookId, limit, forceRefresh);
    }

    private List<ReviewRecommendationResult> generate(Long userId, Long bookContextId, int limit, boolean forceRefresh) {
        if (!forceRefresh && cacheService.exists(userId, bookContextId)) {
            return cacheService.get(userId, bookContextId, limit);
        }

        List<ReviewRecommendationCandidate> candidates = generateCandidates(userId, bookContextId);
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

        List<ReviewRecommendationResult> results = scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
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

        int rank = 1;
        for (ReviewRecommendationResult result : results) {
            result.setRank(rank++);
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
}
