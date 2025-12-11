package org.yyubin.infrastructure.external.book.dto;

public record GoogleBooksSearchRequest(
        String query,
        int startIndex,
        int maxResults,
        String language,
        String orderBy,
        String printType
) {
}
