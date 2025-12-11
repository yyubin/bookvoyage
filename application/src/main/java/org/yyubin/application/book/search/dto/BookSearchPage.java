package org.yyubin.application.book.search.dto;

import java.util.Collections;
import java.util.List;
import org.yyubin.domain.book.BookSearchItem;

public record BookSearchPage(
        List<BookSearchItem> items,
        Integer nextStartIndex,
        int totalItems
) {
    public BookSearchPage {
        items = items == null ? Collections.emptyList() : List.copyOf(items);
        if (totalItems < 0) {
            throw new IllegalArgumentException("totalItems must be greater than or equal to 0");
        }
    }
}
