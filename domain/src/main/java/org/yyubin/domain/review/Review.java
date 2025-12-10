package org.yyubin.domain.review;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Review Aggregate Root
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Review {
    private final ReviewId id;
    private final UserId userId;
    private final BookId bookId;
    private final Rating rating;
    private final String content;
    private final LocalDateTime createdAt;

    public static Review of(ReviewId id, UserId userId, BookId bookId, Rating rating, String content, LocalDateTime createdAt) {
        if (content != null && content.length() > 5000) {
            throw new IllegalArgumentException("Review content cannot exceed 5000 characters");
        }
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(bookId, "Book ID cannot be null");
        Objects.requireNonNull(rating, "Rating cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");

        return new Review(id, userId, bookId, rating, content != null ? content : "", createdAt);
    }

    public static Review create(UserId userId, BookId bookId, Rating rating, String content) {
        return of(null, userId, bookId, rating, content, LocalDateTime.now());
    }

    public Review updateContent(String newContent) {
        return new Review(this.id, this.userId, this.bookId, this.rating, newContent, this.createdAt);
    }

    public Review updateRating(Rating newRating) {
        return new Review(this.id, this.userId, this.bookId, newRating, this.content, this.createdAt);
    }

    public boolean isWrittenBy(UserId userId) {
        return this.userId.equals(userId);
    }

    public boolean isAboutBook(BookId bookId) {
        return this.bookId.equals(bookId);
    }
}
