package org.yyubin.api.book.dto;

import java.util.Collections;
import java.util.List;
import org.yyubin.domain.book.BookSearchItem;

public record BookSearchItemResponse(
        String title,
        List<String> authors,
        String isbn10,
        String isbn13,
        String coverUrl,
        String publisher,
        String publishedDate,
        String description,
        String language,
        Integer pageCount,
        String googleVolumeId
) {

    public static BookSearchItemResponse from(BookSearchItem item) {
        return new BookSearchItemResponse(
                item.getTitle(),
                item.getAuthors() == null ? Collections.emptyList() : item.getAuthors(),
                item.getIsbn10(),
                item.getIsbn13(),
                item.getCoverUrl(),
                item.getPublisher(),
                item.getPublishedDate(),
                item.getDescription(),
                item.getLanguage(),
                item.getPageCount(),
                item.getGoogleVolumeId()
        );
    }
}
