package org.yyubin.infrastructure.external.book.dto;

import java.util.List;

public record GoogleBookVolumeInfoResponse(
        String title,
        List<String> authors,
        String publisher,
        String publishedDate,
        String description,
        Integer pageCount,
        String language,
        GoogleBookImageLinksResponse imageLinks,
        List<GoogleBookIndustryIdentifierResponse> industryIdentifiers
) {
}
