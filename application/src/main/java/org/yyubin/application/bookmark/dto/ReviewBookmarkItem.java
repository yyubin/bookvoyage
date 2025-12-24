package org.yyubin.application.bookmark.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.Review;

public record ReviewBookmarkItem(
        Long bookmarkId,
        Long reviewId,
        LocalDateTime createdAt,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        String publisher,
        String publishedDate,
        int rating,
        String content,
        Long reviewerId,
        String reviewerNickname
) {
    public static ReviewBookmarkItem from(
            Review review,
            Book book,
            Long bookmarkId,
            LocalDateTime createdAt,
            String reviewerNickname
    ) {
        return new ReviewBookmarkItem(
                bookmarkId,
                review.getId().getValue(),
                createdAt,
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthors(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getPublisher(),
                book.getMetadata().getPublishedDate(),
                review.getRating().getValue(),
                review.getContent(),
                review.getUserId().value(),
                reviewerNickname
        );
    }
}
