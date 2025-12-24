package org.yyubin.api.search.dto;

import org.yyubin.api.book.dto.BookSearchResponse;

public record UnifiedSearchResponse(
        String query,
        BookSearchResponse books,
        ReviewSearchPageResponse reviews
) {
    public static UnifiedSearchResponse of(String query, BookSearchResponse books, ReviewSearchPageResponse reviews) {
        return new UnifiedSearchResponse(query, books, reviews);
    }
}
