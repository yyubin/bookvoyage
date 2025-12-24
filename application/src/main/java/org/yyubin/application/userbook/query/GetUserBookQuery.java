package org.yyubin.application.userbook.query;

public record GetUserBookQuery(
        Long userId,
        Long bookId
) {
}
