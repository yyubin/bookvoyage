package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewCircleTopicTest {

    @Test
    void should_create_topic_successfully() {
        // When
        ReviewCircleTopic topic = ReviewCircleTopic.of("실존주의", 5, 8.5);

        // Then
        assertThat(topic.keyword()).isEqualTo("실존주의");
        assertThat(topic.reviewCount()).isEqualTo(5);
        assertThat(topic.score()).isEqualTo(8.5);
        assertThat(topic.lastActivityAt()).isNotNull();
    }

    @Test
    void should_throw_exception_when_keyword_is_blank() {
        // When & Then
        assertThatThrownBy(() -> ReviewCircleTopic.of("", 5, 8.5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("keyword cannot be blank");
    }

    @Test
    void should_throw_exception_when_review_count_is_negative() {
        // When & Then
        assertThatThrownBy(() -> ReviewCircleTopic.of("실존주의", -1, 8.5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("reviewCount cannot be negative");
    }

    @Test
    void should_throw_exception_when_score_is_negative() {
        // When & Then
        assertThatThrownBy(() -> ReviewCircleTopic.of("실존주의", 5, -1.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("score cannot be negative");
    }

    @Test
    void should_sort_by_score_descending() {
        // Given
        ReviewCircleTopic topic1 = ReviewCircleTopic.of("키워드1", 5, 8.5);
        ReviewCircleTopic topic2 = ReviewCircleTopic.of("키워드2", 3, 9.0);
        ReviewCircleTopic topic3 = ReviewCircleTopic.of("키워드3", 7, 7.5);

        // When
        var sorted = java.util.List.of(topic1, topic2, topic3).stream()
            .sorted()
            .toList();

        // Then
        assertThat(sorted.get(0)).isEqualTo(topic2); // score 9.0
        assertThat(sorted.get(1)).isEqualTo(topic1); // score 8.5
        assertThat(sorted.get(2)).isEqualTo(topic3); // score 7.5
    }
}
