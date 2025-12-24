package org.yyubin.api.profile.dto;

import org.yyubin.application.user.dto.FollowUserView;

public record FollowUserItemResponse(
        Long id,
        String name,
        String bio
) {
    public static FollowUserItemResponse from(FollowUserView view) {
        String name = view.nickname() != null && !view.nickname().isBlank()
                ? view.nickname()
                : view.username();
        return new FollowUserItemResponse(
                view.userId(),
                name,
                view.bio()
        );
    }
}
