package org.yyubin.api.book.dto;

import java.util.List;
import org.yyubin.application.book.dto.BookResult;

public record BookResponse(
        Long bookId,
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
        String googleVolumeId,
        BookStatsResponse stats
) {
    public record BookStatsResponse(
            long reviewCount,
            Double avgRating
    ) {
    }

    public static BookResponse from(BookResult result) {
        return new BookResponse(
                result.bookId(),
                result.title(),
                result.authors(),
                result.isbn10(),
                result.isbn13(),
                result.coverUrl(),
                result.publisher(),
                result.publishedDate(),
                result.description(),
                result.language(),
                result.pageCount(),
                result.googleVolumeId(),
                new BookStatsResponse(
                        result.stats().reviewCount(),
                        result.stats().avgRating()
                )
        );
    }
}
