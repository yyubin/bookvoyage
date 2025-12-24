package org.yyubin.application.review.search.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewSearchItemResult(
        Long reviewId,
        Long bookId,
        Long userId,
        String summary,
        List<String> highlights,
        List<String> keywords,
        Integer rating,
        LocalDateTime createdAt
) {
}
