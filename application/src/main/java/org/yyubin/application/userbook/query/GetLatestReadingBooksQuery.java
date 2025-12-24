package org.yyubin.application.userbook.query;

public record GetLatestReadingBooksQuery(Long userId, int size) {
    public GetLatestReadingBooksQuery {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
    }
}
