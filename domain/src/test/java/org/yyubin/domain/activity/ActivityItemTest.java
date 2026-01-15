package org.yyubin.domain.activity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ActivityItem domain tests")
class ActivityItemTest {

    @Test
    @DisplayName("reviewCreated builds item with type and review")
    void reviewCreatedBuildsItem() {
        LocalDateTime createdAt = LocalDateTime.now();

        ActivityItem item = ActivityItem.reviewCreated(1L, new UserId(10L), ReviewId.of(3L), createdAt);

        assertThat(item.getType()).isEqualTo(ActivityType.REVIEW_CREATED);
        assertThat(item.getReviewId()).isEqualTo(ReviewId.of(3L));
        assertThat(item.getTargetUserId()).isNull();
        assertThat(item.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("userFollowed builds item with target user")
    void userFollowedBuildsItem() {
        LocalDateTime createdAt = LocalDateTime.now();
        UserId actorId = new UserId(5L);
        UserId targetId = new UserId(7L);

        ActivityItem item = ActivityItem.userFollowed(2L, actorId, targetId, createdAt);

        assertThat(item.getType()).isEqualTo(ActivityType.USER_FOLLOWED);
        assertThat(item.getReviewId()).isNull();
        assertThat(item.getTargetUserId()).isEqualTo(targetId);
        assertThat(item.getActorId()).isEqualTo(actorId);
    }

    @Test
    @DisplayName("null createdAt is rejected")
    void nullCreatedAtRejected() {
        assertThatThrownBy(() -> ActivityItem.reviewLiked(1L, new UserId(1L), ReviewId.of(2L), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Created at cannot be null");
    }
}
