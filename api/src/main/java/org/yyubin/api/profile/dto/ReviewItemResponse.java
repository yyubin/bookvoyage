package org.yyubin.api.profile.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.api.common.CountFormatter;
import org.yyubin.application.review.dto.ReviewResult;

public record ReviewItemResponse(
        Long id,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        int rating,
        String summary,
        LocalDateTime createdAt,
        String viewCount
) {
    public static ReviewItemResponse from(ReviewResult result) {
        return new ReviewItemResponse(
                result.reviewId(),
                result.bookId(),
                result.title(),
                result.authors(),
                result.coverUrl(),
                result.rating(),
                result.summary(),
                result.createdAt(),
                CountFormatter.format(result.viewCount())
        );
    }
}
