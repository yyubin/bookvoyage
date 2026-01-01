package org.yyubin.application.activity.dto;

public record ActivityActorResult(
        Long userId,
        String username,
        String nickname,
        String profileImageUrl
) {
}
