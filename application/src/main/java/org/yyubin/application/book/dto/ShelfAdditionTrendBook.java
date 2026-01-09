package org.yyubin.application.book.dto;

import java.util.List;

public record ShelfAdditionTrendBook(
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl
) {
}
