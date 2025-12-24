package org.yyubin.application.userbook.query;

public record GetUserBooksQuery(
        Long userId,
        String status
) {
}
