package org.yyubin.domain.bookmark;

import java.time.LocalDateTime;
import java.util.Objects;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

public record ReviewBookmark(
        Long id,
        UserId userId,
        ReviewId reviewId,
        LocalDateTime createdAt
) {
    public ReviewBookmark {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(reviewId, "reviewId must not be null");
        Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static ReviewBookmark create(UserId userId, ReviewId reviewId) {
        return new ReviewBookmark(null, userId, reviewId, LocalDateTime.now());
    }
}
