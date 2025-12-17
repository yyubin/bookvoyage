package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Rating 도메인 테스트")
class RatingTest {

    @Nested
    @DisplayName("Rating 생성")
    class CreateRating {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        @DisplayName("유효한 값(1-5)으로 Rating을 생성할 수 있다")
        void createWithValidRating(int value) {
            // when
            Rating rating = Rating.of(value);

            // then
            assertThat(rating).isNotNull();
            assertThat(rating.getValue()).isEqualTo(value);
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10, 6, 10, 100})
        @DisplayName("범위를 벗어난 값으로 Rating 생성 시 예외가 발생한다")
        void createWithInvalidRating(int invalidValue) {
            // when & then
            assertThatThrownBy(() -> Rating.of(invalidValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Rating must be between 1 and 5");
        }

        @Test
        @DisplayName("최소값 1로 Rating을 생성할 수 있다")
        void createWithMinimumValue() {
            // when
            Rating rating = Rating.of(1);

            // then
            assertThat(rating).isNotNull();
            assertThat(rating.getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("최대값 5로 Rating을 생성할 수 있다")
        void createWithMaximumValue() {
            // when
            Rating rating = Rating.of(5);

            // then
            assertThat(rating).isNotNull();
            assertThat(rating.getValue()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Rating 비즈니스 로직")
    class RatingBusinessLogic {

        @ParameterizedTest
        @ValueSource(ints = {4, 5})
        @DisplayName("평점이 4 이상이면 긍정적(positive)이다")
        void isPositive(int value) {
            // given
            Rating rating = Rating.of(value);

            // when & then
            assertThat(rating.isPositive()).isTrue();
            assertThat(rating.isNegative()).isFalse();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2})
        @DisplayName("평점이 2 이하이면 부정적(negative)이다")
        void isNegative(int value) {
            // given
            Rating rating = Rating.of(value);

            // when & then
            assertThat(rating.isNegative()).isTrue();
            assertThat(rating.isPositive()).isFalse();
        }

        @Test
        @DisplayName("평점이 3이면 긍정도 부정도 아니다")
        void isNeutral() {
            // given
            Rating rating = Rating.of(3);

            // when & then
            assertThat(rating.isPositive()).isFalse();
            assertThat(rating.isNegative()).isFalse();
        }
    }

    @Nested
    @DisplayName("Rating 동등성")
    class RatingEquality {

        @Test
        @DisplayName("같은 값을 가진 Rating은 동등하다")
        void equalRatingsWithSameValue() {
            // given
            Rating rating1 = Rating.of(4);
            Rating rating2 = Rating.of(4);

            // when & then
            assertThat(rating1).isEqualTo(rating2);
            assertThat(rating1.hashCode()).isEqualTo(rating2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 Rating은 동등하지 않다")
        void notEqualRatingsWithDifferentValue() {
            // given
            Rating rating1 = Rating.of(3);
            Rating rating2 = Rating.of(4);

            // when & then
            assertThat(rating1).isNotEqualTo(rating2);
        }

        @Test
        @DisplayName("Rating은 자기 자신과 동등하다")
        void equalToItself() {
            // given
            Rating rating = Rating.of(5);

            // when & then
            assertThat(rating).isEqualTo(rating);
        }

        @Test
        @DisplayName("Rating은 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            Rating rating = Rating.of(5);

            // when & then
            assertThat(rating).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Rating은 다른 타입의 객체와 동등하지 않다")
        void notEqualToDifferentType() {
            // given
            Rating rating = Rating.of(5);
            Integer differentType = 5;

            // when & then
            assertThat(rating).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("Rating toString")
    class RatingToString {

        @Test
        @DisplayName("toString()은 올바른 형식의 문자열을 반환한다")
        void toStringFormat() {
            // given
            Rating rating = Rating.of(4);

            // when
            String result = rating.toString();

            // then
            assertThat(result).isEqualTo("Rating{4}");
        }

        @Test
        @DisplayName("toString()은 rating 값을 포함한다")
        void toStringContainsValue() {
            // given
            Rating rating = Rating.of(5);

            // when
            String result = rating.toString();

            // then
            assertThat(result).contains("5");
        }
    }
}
