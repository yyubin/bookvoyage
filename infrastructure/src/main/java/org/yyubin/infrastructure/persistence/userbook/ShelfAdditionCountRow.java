package org.yyubin.infrastructure.persistence.userbook;

public record ShelfAdditionCountRow(
        Long bookId,
        long addedCount
) {
}
