package org.yyubin.application.recommendation.dto;

import java.util.List;
import org.yyubin.domain.book.Book;

public record BookRecommendationResult(
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
        Double score,
        Integer rank,
        String source,
        String reason
) {
    public static BookRecommendationResult from(Book book, Double score, Integer rank, String source, String reason) {
        return new BookRecommendationResult(
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
                score,
                rank,
                source,
                reason
        );
    }
}
