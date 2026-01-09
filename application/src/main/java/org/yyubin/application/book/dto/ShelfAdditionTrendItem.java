package org.yyubin.application.book.dto;

public record ShelfAdditionTrendItem(
        int rank,
        ShelfAdditionTrendBook book,
        long addedCount
) {
}
