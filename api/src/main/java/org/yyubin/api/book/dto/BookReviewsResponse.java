package org.yyubin.api.book.dto;

import java.util.List;
import org.yyubin.api.common.TimeFormatter;
import org.yyubin.api.common.CountFormatter;
import org.yyubin.application.book.dto.BookReviewsResult;

public record BookReviewsResponse(
        List<ReviewSummaryResponse> reviews,
        Long nextCursor,
        long totalCount
) {
    public record ReviewSummaryResponse(
            Long reviewId,
            Long userId,
            String title,
            Float rating,
            String content,
            String createdAt,
            String likeCount,
            Integer commentCount,
            String viewCount
    ) {
    }

    public static BookReviewsResponse from(BookReviewsResult result) {
        var reviews = result.reviews().stream()
                .map(r -> new ReviewSummaryResponse(
                        r.reviewId(),
                        r.userId(),
                        r.title(),
                        r.rating(),
                        r.content(),
                        TimeFormatter.formatRelativeTime(r.createdAt()),
                        CountFormatter.format(r.likeCount() != null ? r.likeCount() : 0),
                        r.commentCount(),
                        CountFormatter.format(r.viewCount() != null ? r.viewCount() : 0L)
                ))
                .toList();

        return new BookReviewsResponse(reviews, result.nextCursor(), result.totalCount());
    }
}
