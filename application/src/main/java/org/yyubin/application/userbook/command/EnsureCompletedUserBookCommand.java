package org.yyubin.application.userbook.command;

public record EnsureCompletedUserBookCommand(
    Long userId,
    Long bookId
) { }
