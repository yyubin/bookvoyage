package org.yyubin.domain.feed;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * FeedItem Entity representing a review in a user's feed
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FeedItem {
    private final Long id;
    private final UserId userId;
    private final ReviewId reviewId;
    private final LocalDateTime createdAt;

    public static FeedItem of(Long id, UserId userId, ReviewId reviewId, LocalDateTime createdAt) {
        Objects.requireNonNull(userId, "User ID cannot be null");
        Objects.requireNonNull(reviewId, "Review ID cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");

        return new FeedItem(id, userId, reviewId, createdAt);
    }

    public static FeedItem create(UserId userId, ReviewId reviewId) {
        return of(null, userId, reviewId, LocalDateTime.now());
    }

    public boolean belongsTo(UserId userId) {
        return this.userId.equals(userId);
    }

    public boolean isAboutReview(ReviewId reviewId) {
        return this.reviewId.equals(reviewId);
    }
}
