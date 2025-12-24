package org.yyubin.api.search.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.review.search.dto.ReviewSearchItemResult;

public record ReviewSearchItemResponse(
        Long reviewId,
        Long bookId,
        Long userId,
        String summary,
        List<String> highlights,
        List<String> keywords,
        Integer rating,
        LocalDateTime createdAt
) {
    public static ReviewSearchItemResponse from(ReviewSearchItemResult result) {
        return new ReviewSearchItemResponse(
                result.reviewId(),
                result.bookId(),
                result.userId(),
                result.summary(),
                result.highlights(),
                result.keywords(),
                result.rating(),
                result.createdAt()
        );
    }
}
