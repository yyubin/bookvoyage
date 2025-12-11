package org.yyubin.application.review.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewVisibility;

public record ReviewResult(
        Long reviewId,
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
        int rating,
        String content,
        LocalDateTime createdAt,
        ReviewVisibility visibility,
        boolean deleted,
        long viewCount,
        BookGenre genre,
        List<String> keywords,
        List<org.yyubin.domain.review.Mention> mentions
) {

    public static ReviewResult from(Review review, Book book, List<String> keywords) {
        return new ReviewResult(
                review.getId().getValue(),
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
                review.getRating().getValue(),
                review.getContent(),
                review.getCreatedAt(),
                review.getVisibility(),
                review.isDeleted(),
                review.getViewCount(),
                review.getGenre(),
                keywords != null ? keywords : Collections.emptyList(),
                review.getMentions()
        );
    }
}
