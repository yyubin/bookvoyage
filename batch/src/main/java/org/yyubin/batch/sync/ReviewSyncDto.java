package org.yyubin.batch.sync;

import java.time.LocalDateTime;

public record ReviewSyncDto(
        Long id,
        Long userId,
        Long bookId,
        String bookTitle,
        String summary,
        String content,
        java.util.List<String> highlights,
        java.util.List<String> highlightsNorm,
        java.util.List<String> keywords,
        Float rating,
        String visibility,
        LocalDateTime createdAt,
        int likeCount,
        int bookmarkCount,
        int commentCount,
        long viewCount,
        Float dwellScore,
        String genre
) { }
