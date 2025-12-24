package org.yyubin.application.userbook.command;

public record UpdateUserBookMemoCommand(
        Long userId,
        Long bookId,
        String memo
) {
}
