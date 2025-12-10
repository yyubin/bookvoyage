package org.yyubin.application.review;

import java.time.LocalDateTime;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewVisibility;

public record ReviewResult(
        Long reviewId,
        Long bookId,
        String title,
        String author,
        String isbn,
        String coverUrl,
        String description,
        int rating,
        String content,
        LocalDateTime createdAt,
        ReviewVisibility visibility,
        boolean deleted,
        long viewCount,
        BookGenre genre,
        java.util.List<String> keywords,
        java.util.List<org.yyubin.domain.review.Mention> mentions
) {

    public static ReviewResult from(Review review, Book book, java.util.List<String> keywords) {
        return new ReviewResult(
                review.getId().getValue(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthor(),
                book.getMetadata().getIsbn(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getDescription(),
                review.getRating().getValue(),
                review.getContent(),
                review.getCreatedAt(),
                review.getVisibility(),
                review.isDeleted(),
                review.getViewCount(),
                review.getGenre(),
                keywords != null ? keywords : java.util.Collections.emptyList(),
                review.getMentions()
        );
    }
}
