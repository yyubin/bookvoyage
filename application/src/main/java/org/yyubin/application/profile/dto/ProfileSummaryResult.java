package org.yyubin.application.profile.dto;

import java.util.List;

public record ProfileSummaryResult(
        Long id,
        String name,
        String bio,
        String tasteTag,
        String profileImageUrl,
        List<String> tags,
        ProfileStatsResult stats,
        ShelfStatsResult shelves
) {
}
