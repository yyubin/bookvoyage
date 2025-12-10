package org.yyubin.application.user.query;

public record GetFollowingUsersQuery(Long userId, Long cursor, int size) {
    public GetFollowingUsersQuery {
        if (size <= 0) size = 20;
    }
}
