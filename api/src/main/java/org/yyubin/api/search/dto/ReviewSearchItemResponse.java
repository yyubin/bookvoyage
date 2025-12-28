package org.yyubin.api.search.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.api.common.TimeFormatter;
import org.yyubin.application.review.search.dto.ReviewSearchItemResult;

public record ReviewSearchItemResponse(
        Long reviewId,
        Long bookId,
        String bookTitle,
        Long userId,
        String authorNickname,
        String summary,
        List<String> highlights,
        List<String> keywords,
        Integer rating,
        String createdAt
) {
    public static ReviewSearchItemResponse from(ReviewSearchItemResult result) {
        return new ReviewSearchItemResponse(
                result.reviewId(),
                result.bookId(),
                result.bookTitle(),
                result.userId(),
                result.authorNickname(),
                result.summary(),
                result.highlights(),
                result.keywords(),
                result.rating(),
                TimeFormatter.formatRelativeTime(result.createdAt())
        );
    }
}
