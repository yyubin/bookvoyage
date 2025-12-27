package org.yyubin.recommendation.review;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendationIngestCommand(
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
