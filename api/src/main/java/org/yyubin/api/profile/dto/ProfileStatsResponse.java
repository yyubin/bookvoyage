package org.yyubin.api.profile.dto;

import org.yyubin.application.profile.dto.ProfileStatsResult;

public record ProfileStatsResponse(
        long reviews,
        long followers,
        long following
) {
    public static ProfileStatsResponse from(ProfileStatsResult result) {
        return new ProfileStatsResponse(
                result.reviews(),
                result.followers(),
                result.following()
        );
    }
}
