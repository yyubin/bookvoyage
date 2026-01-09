package org.yyubin.application.book.port.dto;

public record ShelfAdditionCount(
        Long bookId,
        long addedCount
) {
}
