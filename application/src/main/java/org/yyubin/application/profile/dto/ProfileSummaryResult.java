package org.yyubin.application.profile.dto;

import java.util.List;

public record ProfileSummaryResult(
        Long id,
        String name,
        String bio,
        List<String> tags,
        ProfileStatsResult stats,
        ShelfStatsResult shelves
) {
}
