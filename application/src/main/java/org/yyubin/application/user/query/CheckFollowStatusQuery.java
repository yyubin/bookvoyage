package org.yyubin.application.user.query;

public record CheckFollowStatusQuery(
        Long followerId,
        Long targetUserId
) {
}
