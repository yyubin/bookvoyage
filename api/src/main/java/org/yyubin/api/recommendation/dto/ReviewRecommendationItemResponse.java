package org.yyubin.api.recommendation.dto;

import org.yyubin.api.common.TimeFormatter;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;

public record ReviewRecommendationItemResponse(
        Long reviewId,
        Long userId,
        Long bookId,
        String summary,
        String content,
        Integer rating,
        String createdAt,
        Double score,
        Integer rank,
        String source,
        String reason
) {
    public static ReviewRecommendationItemResponse from(ReviewRecommendationResultDto result) {
        return new ReviewRecommendationItemResponse(
                result.reviewId(),
                result.userId(),
                result.bookId(),
                result.summary(),
                result.content(),
                result.rating(),
                TimeFormatter.formatRelativeTime(result.createdAt()),
                result.score(),
                result.rank(),
                result.source(),
                result.reason()
        );
    }
}
