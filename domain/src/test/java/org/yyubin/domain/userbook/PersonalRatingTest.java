package org.yyubin.domain.userbook;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PersonalRatingTest {

    @Test
    @DisplayName("유효한 평점으로 생성")
    void of_withValidRating() {
        // when
        PersonalRating rating = PersonalRating.of(5);

        // then
        assertThat(rating.getValue()).isEqualTo(5);
        assertThat(rating.hasRating()).isTrue();
    }

    @Test
    @DisplayName("null 평점으로 생성")
    void of_withNullRating() {
        // when
        PersonalRating rating = PersonalRating.of(null);

        // then
        assertThat(rating.getValue()).isNull();
        assertThat(rating.hasRating()).isFalse();
    }

    @Test
    @DisplayName("빈 평점 생성")
    void empty() {
        // when
        PersonalRating rating = PersonalRating.empty();

        // then
        assertThat(rating.getValue()).isNull();
        assertThat(rating.hasRating()).isFalse();
    }

    @Test
    @DisplayName("평점이 1보다 작으면 예외 발생")
    void of_withRatingLessThan1_throwsException() {
        assertThatThrownBy(() -> PersonalRating.of(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rating must be between 1 and 5");
    }

    @Test
    @DisplayName("평점이 5보다 크면 예외 발생")
    void of_withRatingGreaterThan5_throwsException() {
        assertThatThrownBy(() -> PersonalRating.of(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Rating must be between 1 and 5");
    }
}
