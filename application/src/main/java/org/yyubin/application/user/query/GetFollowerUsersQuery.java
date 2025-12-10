package org.yyubin.application.user.query;

public record GetFollowerUsersQuery(Long userId, Long cursor, int size) {
    public GetFollowerUsersQuery {
        if (size <= 0) size = 20;
    }
}
