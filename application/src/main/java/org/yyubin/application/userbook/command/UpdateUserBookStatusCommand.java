package org.yyubin.application.userbook.command;

public record UpdateUserBookStatusCommand(
        Long userId,
        Long bookId,
        String status
) {
}
