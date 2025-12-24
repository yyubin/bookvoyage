package org.yyubin.application.profile.dto;

public record ProfileStatsResult(
        long reviews,
        long followers,
        long following
) {
}
