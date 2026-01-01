package org.yyubin.api.activity.dto;

import org.yyubin.application.activity.dto.ActivityActorResult;

public record ActivityActorResponse(
        Long userId,
        String username,
        String nickname,
        String profileImageUrl
) {
    public static ActivityActorResponse from(ActivityActorResult result) {
        return new ActivityActorResponse(
                result.userId(),
                result.username(),
                result.nickname(),
                result.profileImageUrl()
        );
    }
}
