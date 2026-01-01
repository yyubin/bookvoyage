package org.yyubin.domain.activity;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

@Getter
@ToString
@EqualsAndHashCode(of = {"id", "type"})
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ActivityItem {
    private final Long id;
    private final ActivityType type;
    private final UserId actorId;
    private final ReviewId reviewId;
    private final UserId targetUserId;
    private final LocalDateTime createdAt;

    public static ActivityItem reviewCreated(Long id, UserId actorId, ReviewId reviewId, LocalDateTime createdAt) {
        return new ActivityItem(id, ActivityType.REVIEW_CREATED, actorId, reviewId, null, requireCreatedAt(createdAt));
    }

    public static ActivityItem reviewLiked(Long id, UserId actorId, ReviewId reviewId, LocalDateTime createdAt) {
        return new ActivityItem(id, ActivityType.REVIEW_LIKED, actorId, reviewId, null, requireCreatedAt(createdAt));
    }

    public static ActivityItem reviewBookmarked(Long id, UserId actorId, ReviewId reviewId, LocalDateTime createdAt) {
        return new ActivityItem(id, ActivityType.REVIEW_BOOKMARKED, actorId, reviewId, null, requireCreatedAt(createdAt));
    }

    public static ActivityItem userFollowed(Long id, UserId actorId, UserId targetUserId, LocalDateTime createdAt) {
        return new ActivityItem(id, ActivityType.USER_FOLLOWED, actorId, null, targetUserId, requireCreatedAt(createdAt));
    }

    private static LocalDateTime requireCreatedAt(LocalDateTime createdAt) {
        return Objects.requireNonNull(createdAt, "Created at cannot be null");
    }
}
