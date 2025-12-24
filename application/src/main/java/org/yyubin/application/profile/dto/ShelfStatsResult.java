package org.yyubin.application.profile.dto;

public record ShelfStatsResult(
        long reading,
        long finished,
        long savedReviews,
        long bookmarks
) {
}
