package org.yyubin.application.userbook.command;

public record DeleteUserBookCommand(
        Long userId,
        Long bookId
) {
}
