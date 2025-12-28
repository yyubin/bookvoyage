package org.yyubin.application.book.dto;

import java.util.List;

public record BookReviewsResult(
        List<ReviewSummary> reviews,
        Long nextCursor,
        long totalCount
) {
    public record ReviewSummary(
            Long reviewId,
            Long userId,
            String title,
            Float rating,
            String content,
            java.time.LocalDateTime createdAt,
            Integer likeCount,
            Integer commentCount,
            Long viewCount
    ) {
    }
}
