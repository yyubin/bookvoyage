package org.yyubin.api.bookmark.dto;

import java.time.LocalDateTime;

public record BookmarkActionResponse(
        Long reviewId,
        boolean bookmarked,
        LocalDateTime createdAt
) {
    public static BookmarkActionResponse bookmarked(Long reviewId, LocalDateTime createdAt) {
        return new BookmarkActionResponse(reviewId, true, createdAt);
    }
}
