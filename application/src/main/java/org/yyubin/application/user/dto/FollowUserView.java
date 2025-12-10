package org.yyubin.application.user.dto;

public record FollowUserView(
        Long userId,
        String username,
        String nickname,
        String profileImageUrl
) {
}
