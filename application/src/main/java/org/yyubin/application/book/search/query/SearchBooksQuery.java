package org.yyubin.application.book.search.query;

public record SearchBooksQuery(
        String keyword,
        Integer startIndex,
        Integer size,
        String language,
        SearchOrder orderBy,
        PrintType printType
) {
}
