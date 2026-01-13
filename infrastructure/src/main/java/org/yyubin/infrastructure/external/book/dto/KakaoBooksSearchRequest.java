package org.yyubin.infrastructure.external.book.dto;

public record KakaoBooksSearchRequest(
        String query,
        String sort,      // accuracy | latest
        Integer page,     // 1-50
        Integer size,     // 1-50
        String target     // title | isbn | publisher | person
) {
}
