package org.yyubin.api.user.dto;

import org.yyubin.application.user.dto.FollowUserView;

public record FollowUserResponse(
        Long userId,
        String username,
        String nickname,
        String profileImageUrl
) {

    public static FollowUserResponse from(FollowUserView view) {
        return new FollowUserResponse(
                view.userId(),
                view.username(),
                view.nickname(),
                view.profileImageUrl()
        );
    }
}
