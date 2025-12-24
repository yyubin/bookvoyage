package org.yyubin.domain.review;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private final String summary;
    private final String content;
    private final LocalDateTime createdAt;
    private final ReviewVisibility visibility;
    private final boolean deleted;
    private final long viewCount;
    private final BookGenre genre;
    private final List<Mention> mentions;

    private static final int MAX_SUMMARY_LENGTH = 200;

    public static Review of(
            ReviewId id,
            UserId userId,
            BookId bookId,
            Rating rating,
            String summary,
            String content,
            LocalDateTime createdAt,
            ReviewVisibility visibility,
            boolean deleted,
            long viewCount,
            BookGenre genre,
            List<Mention> mentions
    ) {
        if (summary != null && summary.length() > MAX_SUMMARY_LENGTH) {
            throw new IllegalArgumentException("Review summary cannot exceed " + MAX_SUMMARY_LENGTH + " characters");
        }
        if (content != null && content.length() > 5000) {
            throw new IllegalArgumentException("Review content cannot exceed 5000 characters");
        }
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(bookId, "Book ID cannot be null");
        Objects.requireNonNull(rating, "Rating cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");
        Objects.requireNonNull(visibility, "Visibility cannot be null");
        if (genre == null) {
            throw new IllegalArgumentException("Genre cannot be null");
        }
        Objects.requireNonNull(mentions, "Mentions cannot be null");

        long safeViewCount = Math.max(0, viewCount);

        return new Review(
                id,
                userId,
                bookId,
                rating,
                summary != null ? summary : "",
                content != null ? content : "",
                createdAt,
                visibility,
                deleted,
                safeViewCount,
                genre,
                List.copyOf(mentions)
        );
    }

    public static Review of(
            ReviewId id,
            UserId userId,
            BookId bookId,
            Rating rating,
            String content,
            LocalDateTime createdAt,
            ReviewVisibility visibility,
            boolean deleted,
            long viewCount,
            BookGenre genre,
            List<Mention> mentions
    ) {
        return of(
                id,
                userId,
                bookId,
                rating,
                null,
                content,
                createdAt,
                visibility,
                deleted,
                viewCount,
                genre,
                mentions
        );
    }

    public static Review create(
            UserId userId,
            BookId bookId,
            Rating rating,
            String summary,
            String content,
            ReviewVisibility visibility,
            BookGenre genre,
            List<Mention> mentions
    ) {
        return of(
                null,
                userId,
                bookId,
                rating,
                summary,
                content,
                LocalDateTime.now(),
                visibility != null ? visibility : ReviewVisibility.PUBLIC,
                false,
                0,
                genre,
                mentions != null ? mentions : Collections.emptyList()
        );
    }

    public static Review create(
            UserId userId,
            BookId bookId,
            Rating rating,
            String content,
            ReviewVisibility visibility,
            BookGenre genre,
            List<Mention> mentions
    ) {
        return create(userId, bookId, rating, null, content, visibility, genre, mentions);
    }

    public Review updateContent(String newContent, List<Mention> newMentions) {
        Objects.requireNonNull(newMentions, "Mentions cannot be null");
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                this.rating,
                this.summary,
                newContent,
                this.createdAt,
                this.visibility,
                this.deleted,
                this.viewCount,
                this.genre,
                List.copyOf(newMentions)
        );
    }

    public Review updateRating(Rating newRating) {
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                newRating,
                this.summary,
                this.content,
                this.createdAt,
                this.visibility,
                this.deleted,
                this.viewCount,
                this.genre,
                this.mentions
        );
    }

    public Review updateSummary(String newSummary) {
        if (newSummary != null && newSummary.length() > MAX_SUMMARY_LENGTH) {
            throw new IllegalArgumentException("Review summary cannot exceed " + MAX_SUMMARY_LENGTH + " characters");
        }
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                this.rating,
                newSummary != null ? newSummary : "",
                this.content,
                this.createdAt,
                this.visibility,
                this.deleted,
                this.viewCount,
                this.genre,
                this.mentions
        );
    }

    public Review updateVisibility(ReviewVisibility newVisibility) {
        Objects.requireNonNull(newVisibility, "Visibility cannot be null");
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                this.rating,
                this.summary,
                this.content,
                this.createdAt,
                newVisibility,
                this.deleted,
                this.viewCount,
                this.genre,
                this.mentions
        );
    }

    public Review updateGenre(BookGenre newGenre) {
        Objects.requireNonNull(newGenre, "Genre cannot be null");
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                this.rating,
                this.summary,
                this.content,
                this.createdAt,
                this.visibility,
                this.deleted,
                this.viewCount,
                newGenre,
                this.mentions
        );
    }

    public Review markDeleted() {
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                this.rating,
                this.summary,
                this.content,
                this.createdAt,
                this.visibility,
                true,
                this.viewCount,
                this.genre,
                this.mentions
        );
    }

    public Review increaseViewCount() {
        return new Review(
                this.id,
                this.userId,
                this.bookId,
                this.rating,
                this.summary,
                this.content,
                this.createdAt,
                this.visibility,
                this.deleted,
                this.viewCount + 1,
                this.genre,
                this.mentions
        );
    }

    public boolean isWrittenBy(UserId userId) {
        return this.userId.equals(userId);
    }

    public boolean isAboutBook(BookId bookId) {
        return this.bookId.equals(bookId);
    }
}
