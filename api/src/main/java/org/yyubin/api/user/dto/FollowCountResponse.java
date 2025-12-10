package org.yyubin.api.user.dto;

import org.yyubin.application.user.dto.FollowCountResult;

public record FollowCountResponse(
        Long followingCount,
        Long followerCount
) {

    public static FollowCountResponse onlyFollowing(FollowCountResult result) {
        return new FollowCountResponse(result.followingCount(), null);
    }

    public static FollowCountResponse onlyFollowers(FollowCountResult result) {
        return new FollowCountResponse(null, result.followerCount());
    }
}
