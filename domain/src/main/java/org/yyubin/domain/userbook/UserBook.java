package org.yyubin.domain.userbook;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = {"userId", "bookId"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserBook {
    private final Long id;
    private final UserId userId;
    private final BookId bookId;
    private final ReadingStatus status;
    private final ReadingProgress progress;
    private final PersonalRating rating;
    private final PersonalMemo memo;
    private final ReadingCount readingCount;
    private final LocalDateTime startDate;
    private final LocalDateTime completionDate;
    private final boolean deleted;
    private final LocalDateTime deletedAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserBook create(UserId userId, BookId bookId, ReadingStatus status) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");
        Objects.requireNonNull(status, "status must not be null");

        LocalDateTime now = LocalDateTime.now();
        return new UserBook(
                null,
                userId,
                bookId,
                status,
                ReadingProgress.notStarted(),
                PersonalRating.empty(),
                PersonalMemo.empty(),
                ReadingCount.first(),
                null,
                null,
                false,
                null,
                now,
                now
        );
    }

    public static UserBook of(
            Long id,
            UserId userId,
            BookId bookId,
            ReadingStatus status,
            ReadingProgress progress,
            PersonalRating rating,
            PersonalMemo memo,
            ReadingCount readingCount,
            LocalDateTime startDate,
            LocalDateTime completionDate,
            boolean deleted,
            LocalDateTime deletedAt,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(bookId, "bookId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(progress, "progress must not be null");
        Objects.requireNonNull(rating, "rating must not be null");
        Objects.requireNonNull(memo, "memo must not be null");
        Objects.requireNonNull(readingCount, "readingCount must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");

        return new UserBook(
                id, userId, bookId, status, progress, rating, memo,
                readingCount, startDate, completionDate, deleted, deletedAt,
                createdAt, updatedAt
        );
    }

    // State transition methods

    public UserBook startReading() {
        if (status == ReadingStatus.READING) {
            return this;
        }

        LocalDateTime now = LocalDateTime.now();
        ReadingCount newReadingCount = this.readingCount;
        ReadingProgress newProgress = ReadingProgress.notStarted();

        // If transitioning from COMPLETED, increment reading count and reset progress
        if (status == ReadingStatus.COMPLETED) {
            newReadingCount = readingCount.increment();
        }

        return new UserBook(
                id, userId, bookId,
                ReadingStatus.READING,
                newProgress,
                rating,
                memo,
                newReadingCount,
                now,
                null,  // Clear completion date
                deleted,
                deletedAt,
                createdAt,
                now
        );
    }

    public UserBook markAsCompleted() {
        if (status == ReadingStatus.COMPLETED) {
            return this;
        }

        LocalDateTime now = LocalDateTime.now();
        return new UserBook(
                id, userId, bookId,
                ReadingStatus.COMPLETED,
                ReadingProgress.completed(),  // Auto-set to 100%
                rating,
                memo,
                readingCount,
                startDate,
                now,  // Set completion date
                deleted,
                deletedAt,
                createdAt,
                now
        );
    }

    public UserBook moveToWantToRead() {
        if (status == ReadingStatus.WANT_TO_READ) {
            return this;
        }

        LocalDateTime now = LocalDateTime.now();
        return new UserBook(
                id, userId, bookId,
                ReadingStatus.WANT_TO_READ,
                ReadingProgress.notStarted(),
                rating,
                memo,
                readingCount,
                null,  // Clear dates
                null,
                deleted,
                deletedAt,
                createdAt,
                now
        );
    }

    public UserBook updateProgress(int progressPercent) {
        if (status != ReadingStatus.READING) {
            throw new IllegalStateException("Progress can only be updated when status is READING");
        }

        ReadingProgress newProgress = ReadingProgress.of(progressPercent);
        LocalDateTime now = LocalDateTime.now();

        return new UserBook(
                id, userId, bookId,
                status,
                newProgress,
                rating,
                memo,
                readingCount,
                startDate,
                completionDate,
                deleted,
                deletedAt,
                createdAt,
                now
        );
    }

    public UserBook rate(Integer ratingValue) {
        PersonalRating newRating = PersonalRating.of(ratingValue);
        LocalDateTime now = LocalDateTime.now();

        return new UserBook(
                id, userId, bookId,
                status,
                progress,
                newRating,
                memo,
                readingCount,
                startDate,
                completionDate,
                deleted,
                deletedAt,
                createdAt,
                now
        );
    }

    public UserBook updateMemo(String memoText) {
        PersonalMemo newMemo = PersonalMemo.of(memoText);
        LocalDateTime now = LocalDateTime.now();

        return new UserBook(
                id, userId, bookId,
                status,
                progress,
                rating,
                newMemo,
                readingCount,
                startDate,
                completionDate,
                deleted,
                deletedAt,
                createdAt,
                now
        );
    }

    public UserBook delete() {
        if (deleted) {
            return this;
        }

        LocalDateTime now = LocalDateTime.now();
        return new UserBook(
                id, userId, bookId,
                status,
                progress,
                rating,
                memo,
                readingCount,
                startDate,
                completionDate,
                true,
                now,
                createdAt,
                now
        );
    }

    public UserBook restore() {
        if (!deleted) {
            return this;
        }

        LocalDateTime now = LocalDateTime.now();
        return new UserBook(
                id, userId, bookId,
                status,
                progress,
                rating,
                memo,
                readingCount,
                startDate,
                completionDate,
                false,
                null,
                createdAt,
                now
        );
    }

    // Query methods

    public boolean isReading() {
        return status == ReadingStatus.READING;
    }

    public boolean isCompleted() {
        return status == ReadingStatus.COMPLETED;
    }

    public boolean isWantToRead() {
        return status == ReadingStatus.WANT_TO_READ;
    }

    public boolean canMarkCompleted() {
        return status == ReadingStatus.READING;
    }

    public boolean hasRating() {
        return rating.hasRating();
    }

    public boolean hasMemo() {
        return memo.hasContent();
    }

    public boolean isDeleted() {
        return deleted;
    }
}
