package org.yyubin.application.userbook.command;

public record UpdateUserBookProgressCommand(
        Long userId,
        Long bookId,
        int progress
) {
}
