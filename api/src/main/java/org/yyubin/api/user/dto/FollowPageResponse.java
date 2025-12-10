package org.yyubin.api.user.dto;

import java.util.List;
import org.yyubin.application.user.dto.FollowPageResult;

public record FollowPageResponse(
        List<FollowUserResponse> users,
        Long nextCursor
) {

    public static FollowPageResponse from(FollowPageResult result) {
        return new FollowPageResponse(
                result.users().stream().map(FollowUserResponse::from).toList(),
                result.nextCursor()
        );
    }
}
