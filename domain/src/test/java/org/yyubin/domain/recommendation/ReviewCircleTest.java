package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewCircle record tests")
class ReviewCircleTest {

    @Test
    @DisplayName("constructor rejects negative similarUserCount")
    void negativeSimilarUserCountRejected() {
        assertThatThrownBy(() -> new ReviewCircle(1L, "24h", List.of(), -1, LocalDateTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("getTopTopics sorts by score")
    void getTopTopicsSorts() {
        ReviewCircleTopic low = ReviewCircleTopic.of("a", 1, 0.1, LocalDateTime.now());
        ReviewCircleTopic high = ReviewCircleTopic.of("b", 2, 0.9, LocalDateTime.now());

        ReviewCircle circle = ReviewCircle.of(1L, "24h", List.of(low, high), 2);

        List<ReviewCircleTopic> top = circle.getTopTopics(1);
        assertThat(top).containsExactly(high);
    }
}
