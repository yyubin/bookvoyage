package org.yyubin.api.user.dto;

import org.yyubin.api.common.CountFormatter;
import org.yyubin.application.user.dto.FollowCountResult;

public record FollowCountResponse(
        String followingCount,
        String followerCount
) {

    public static FollowCountResponse onlyFollowing(FollowCountResult result) {
        return new FollowCountResponse(CountFormatter.format(result.followingCount()), null);
    }

    public static FollowCountResponse onlyFollowers(FollowCountResult result) {
        return new FollowCountResponse(null, CountFormatter.format(result.followerCount()));
    }
}
