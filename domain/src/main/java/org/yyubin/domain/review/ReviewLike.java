package org.yyubin.domain.review;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.yyubin.domain.user.UserId;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewLike {

    private final ReviewLikeId id;
    private final ReviewId reviewId;
    private final UserId userId;
    private final LocalDateTime createdAt;

    public static ReviewLike create(ReviewId reviewId, UserId userId) {
        return new ReviewLike(
                null,
                reviewId,
                userId,
                LocalDateTime.now()
        );
    }

    public static ReviewLike reconstruct(ReviewLikeId id, ReviewId reviewId, UserId userId, LocalDateTime createdAt) {
        return new ReviewLike(id, reviewId, userId, createdAt);
    }
}
