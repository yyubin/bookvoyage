package org.yyubin.domain.feed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FeedItem domain tests")
class FeedItemTest {

    @Test
    @DisplayName("of validates required fields")
    void ofValidatesFields() {
        assertThatThrownBy(() -> FeedItem.of(1L, null, ReviewId.of(2L), LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("create builds item and helpers match")
    void createBuildsItem() {
        UserId userId = new UserId(3L);
        ReviewId reviewId = ReviewId.of(4L);

        FeedItem item = FeedItem.create(userId, reviewId);

        assertThat(item.getId()).isNull();
        assertThat(item.belongsTo(userId)).isTrue();
        assertThat(item.isAboutReview(reviewId)).isTrue();
    }
}
