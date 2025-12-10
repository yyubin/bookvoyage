package org.yyubin.application.user.command;

public record ToggleFollowCommand(
        Long followerId,
        Long targetUserId
) {
}
