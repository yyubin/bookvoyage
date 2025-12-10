package org.yyubin.application.user.query;

public record GetUserProfileQuery(
        Long userId
) {
    public GetUserProfileQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
}
