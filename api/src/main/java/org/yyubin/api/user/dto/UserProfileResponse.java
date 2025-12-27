package org.yyubin.api.user.dto;

import org.yyubin.application.user.query.UserProfileResult;

public record UserProfileResponse(
        Long userId,
        String email,
        String username,
        String nickname,
        String bio,
        String tasteTag,
        String provider
) {
    public static UserProfileResponse from(UserProfileResult result) {
        return new UserProfileResponse(
                result.userId(),
                result.email(),
                result.username(),
                result.nickname(),
                result.bio(),
                result.tasteTag(),
                result.provider()
        );
    }
}
