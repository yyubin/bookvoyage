package org.yyubin.application.recommendation.dto;

import java.time.LocalDateTime;
import org.yyubin.domain.review.Review;

public record ReviewRecommendationResultDto(
        Long reviewId,
        Long userId,
        Long bookId,
        String summary,
        String content,
        Integer rating,
        LocalDateTime createdAt,
        Double score,
        Integer rank,
        String source,
        String reason
) {
    public static ReviewRecommendationResultDto from(
            Review review,
            Double score,
            Integer rank,
            String source,
            String reason
    ) {
        return new ReviewRecommendationResultDto(
                review.getId() != null ? review.getId().getValue() : null,
                review.getUserId().value(),
                review.getBookId().getValue(),
                review.getSummary(),
                review.getContent(),
                review.getRating().getValue(),
                review.getCreatedAt(),
                score,
                rank,
                source,
                reason
        );
    }
}
