package org.yyubin.api.profile.dto;

import org.yyubin.application.profile.dto.ShelfStatsResult;

public record ShelfStatsResponse(
        long reading,
        long finished,
        long savedReviews,
        long bookmarks
) {
    public static ShelfStatsResponse from(ShelfStatsResult result) {
        return new ShelfStatsResponse(
                result.reading(),
                result.finished(),
                result.savedReviews(),
                result.bookmarks()
        );
    }
}
