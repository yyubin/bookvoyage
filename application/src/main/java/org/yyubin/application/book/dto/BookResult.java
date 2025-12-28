package org.yyubin.application.book.dto;

import java.util.List;
import org.yyubin.domain.book.Book;

public record BookResult(
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
        BookStats stats
) {
    public record BookStats(
            long reviewCount,
            Double avgRating
    ) {
    }

    public static BookResult from(Book book, long reviewCount, Double avgRating) {
        return new BookResult(
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthors(),
                book.getMetadata().getIsbn10(),
                book.getMetadata().getIsbn13(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getPublisher(),
                book.getMetadata().getPublishedDate(),
                book.getMetadata().getDescription(),
                book.getMetadata().getLanguage(),
                book.getMetadata().getPageCount(),
                book.getMetadata().getGoogleVolumeId(),
                new BookStats(reviewCount, avgRating)
        );
    }
}
