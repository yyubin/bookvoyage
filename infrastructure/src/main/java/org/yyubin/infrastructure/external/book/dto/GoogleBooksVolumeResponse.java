package org.yyubin.infrastructure.external.book.dto;

import java.util.List;

public record GoogleBooksVolumeResponse(
        Integer totalItems,
        List<GoogleBookItemResponse> items
) {
}
