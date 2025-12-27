package org.yyubin.api.profile.dto;

import java.util.List;
import org.yyubin.application.profile.dto.ProfileSummaryResult;

public record ProfileSummaryResponse(
        Long id,
        String name,
        String bio,
        String tasteTag,
        String profileImageUrl,
        List<String> tags,
        ProfileStatsResponse stats,
        ShelfStatsResponse shelves
) {
    public static ProfileSummaryResponse from(ProfileSummaryResult result) {
        return new ProfileSummaryResponse(
                result.id(),
                result.name(),
                result.bio(),
                result.tasteTag(),
                result.profileImageUrl(),
                result.tags(),
                ProfileStatsResponse.from(result.stats()),
                ShelfStatsResponse.from(result.shelves())
        );
    }
}
