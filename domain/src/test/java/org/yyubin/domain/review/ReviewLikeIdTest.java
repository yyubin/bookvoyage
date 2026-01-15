package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewLikeId record tests")
class ReviewLikeIdTest {

    @Test
    @DisplayName("of creates id")
    void ofCreatesId() {
        ReviewLikeId id = ReviewLikeId.of(4L);
        assertThat(id.value()).isEqualTo(4L);
    }
}
