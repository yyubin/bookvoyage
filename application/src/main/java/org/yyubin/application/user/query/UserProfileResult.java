package org.yyubin.application.user.query;

import org.yyubin.domain.user.User;

public record UserProfileResult(
        Long userId,
        String email,
        String username,
        String nickname,
        String bio,
        String provider,
        String profileImageUrl
) {
    public static UserProfileResult from(User user) {
        return new UserProfileResult(
                user.id().value(),
                user.email(),
                user.username(),
                user.nickname(),
                user.bio(),
                user.provider().name(),
                user.ProfileImageUrl()
        );
    }
}
