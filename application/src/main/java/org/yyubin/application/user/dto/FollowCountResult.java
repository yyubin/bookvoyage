package org.yyubin.application.user.dto;

public record FollowCountResult(
        long followingCount,
        long followerCount
) {
}
