package org.yyubin.api.book.dto;

import java.util.List;
import org.yyubin.application.book.search.dto.BookSearchPage;

public record BookSearchResponse(
        List<BookSearchItemResponse> items,
        Integer nextStartIndex,
        int totalItems
) {
    public static BookSearchResponse from(BookSearchPage page) {
        List<BookSearchItemResponse> mapped = page.items().stream()
                .map(BookSearchItemResponse::from)
                .toList();

        return new BookSearchResponse(mapped, page.nextStartIndex(), page.totalItems());
    }
}
