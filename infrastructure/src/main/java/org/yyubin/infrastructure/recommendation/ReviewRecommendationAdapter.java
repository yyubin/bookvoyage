package org.yyubin.infrastructure.recommendation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.ReviewRecommendationPort;
import org.yyubin.recommendation.service.ReviewRecommendationResult;
import org.yyubin.recommendation.service.ReviewRecommendationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRecommendationAdapter implements ReviewRecommendationPort {

    private final ReviewRecommendationService reviewRecommendationService;

    @Override
    public List<RecommendationItem> getRecommendations(Long userId, Long cursor, int limit, boolean forceRefresh) {
        List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(
                userId,
                cursor,
                limit,
                forceRefresh
        );

        return results.stream()
                .map(r -> new RecommendationItem(
                        r.getReviewId(),
                        r.getBookId(),
                        r.getScore(),
                        r.getRank(),
                        r.getSource(),
                        r.getReason(),
                        r.getCreatedAt()
                ))
                .toList();
    }
}
