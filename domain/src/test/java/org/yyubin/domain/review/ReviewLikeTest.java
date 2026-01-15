package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewLike domain tests")
class ReviewLikeTest {

    @Test
    @DisplayName("create sets null id and timestamp")
    void createSetsDefaults() {
        ReviewLike like = ReviewLike.create(ReviewId.of(1L), new UserId(2L));

        assertThat(like.getId()).isNull();
        assertThat(like.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("reconstruct keeps provided values")
    void reconstructKeepsValues() {
        LocalDateTime createdAt = LocalDateTime.now();
        ReviewLike like = ReviewLike.reconstruct(ReviewLikeId.of(3L), ReviewId.of(1L), new UserId(2L), createdAt);

        assertThat(like.getId()).isEqualTo(ReviewLikeId.of(3L));
        assertThat(like.getCreatedAt()).isEqualTo(createdAt);
    }
}
