package org.yyubin.application.review.search.event;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewSearchIndexEvent(
        ReviewSearchIndexEventType type,
        Long reviewId,
        Long userId,
        Long bookId,
        String bookTitle,
        String summary,
        String content,
        List<String> highlights,
        List<String> highlightsNorm,
        List<String> keywords,
        String genre,
        LocalDateTime createdAt,
        Integer rating
) {
}
