package org.yyubin.application.userbook.command;

public record UpdateUserBookRatingCommand(
        Long userId,
        Long bookId,
        Integer rating
) {
}
