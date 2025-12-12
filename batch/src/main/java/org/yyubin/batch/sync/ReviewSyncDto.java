package org.yyubin.batch.sync;

import java.time.LocalDateTime;

public record ReviewSyncDto(
        Long id,
        Long userId,
        Long bookId,
        String content,
        Float rating,
        String visibility,
        LocalDateTime createdAt,
        int likeCount,
        int bookmarkCount,
        int commentCount,
        long viewCount,
        Float dwellScore
) { }
