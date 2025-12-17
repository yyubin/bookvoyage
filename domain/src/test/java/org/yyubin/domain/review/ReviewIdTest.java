package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewId 도메인 테스트")
class ReviewIdTest {

    @Nested
    @DisplayName("ReviewId 생성")
    class CreateReviewId {

        @Test
        @DisplayName("유효한 양수 ID로 ReviewId를 생성할 수 있다")
        void createWithValidId() {
            // given
            Long validId = 1L;

            // when
            ReviewId reviewId = ReviewId.of(validId);

            // then
            assertThat(reviewId).isNotNull();
            assertThat(reviewId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("큰 숫자의 양수 ID로 ReviewId를 생성할 수 있다")
        void createWithLargeId() {
            // given
            Long largeId = Long.MAX_VALUE;

            // when
            ReviewId reviewId = ReviewId.of(largeId);

            // then
            assertThat(reviewId).isNotNull();
            assertThat(reviewId.getValue()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("null ID로 ReviewId 생성 시 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> ReviewId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Review ID must be positive");
        }

        @Test
        @DisplayName("0으로 ReviewId 생성 시 예외가 발생한다")
        void createWithZeroId() {
            // when & then
            assertThatThrownBy(() -> ReviewId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Review ID must be positive");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -10L, -100L, Long.MIN_VALUE})
        @DisplayName("음수 ID로 ReviewId 생성 시 예외가 발생한다")
        void createWithNegativeId(Long negativeId) {
            // when & then
            assertThatThrownBy(() -> ReviewId.of(negativeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Review ID must be positive");
        }
    }

    @Nested
    @DisplayName("ReviewId 동등성")
    class ReviewIdEquality {

        @Test
        @DisplayName("같은 값을 가진 ReviewId는 동등하다")
        void equalReviewIdsWithSameValue() {
            // given
            ReviewId reviewId1 = ReviewId.of(1L);
            ReviewId reviewId2 = ReviewId.of(1L);

            // when & then
            assertThat(reviewId1).isEqualTo(reviewId2);
            assertThat(reviewId1.hashCode()).isEqualTo(reviewId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ReviewId는 동등하지 않다")
        void notEqualReviewIdsWithDifferentValue() {
            // given
            ReviewId reviewId1 = ReviewId.of(1L);
            ReviewId reviewId2 = ReviewId.of(2L);

            // when & then
            assertThat(reviewId1).isNotEqualTo(reviewId2);
        }

        @Test
        @DisplayName("ReviewId는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            ReviewId reviewId = ReviewId.of(1L);

            // when & then
            assertThat(reviewId).isEqualTo(reviewId);
        }

        @Test
        @DisplayName("ReviewId는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            ReviewId reviewId = ReviewId.of(1L);

            // when & then
            assertThat(reviewId).isNotEqualTo(null);
        }

        @Test
        @DisplayName("ReviewId는 다른 타입의 객체와 동등하지 않다")
        void notEqualToDifferentType() {
            // given
            ReviewId reviewId = ReviewId.of(1L);
            String differentType = "1";

            // when & then
            assertThat(reviewId).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("ReviewId toString")
    class ReviewIdToString {

        @Test
        @DisplayName("toString()은 올바른 형식의 문자열을 반환한다")
        void toStringFormat() {
            // given
            ReviewId reviewId = ReviewId.of(123L);

            // when
            String result = reviewId.toString();

            // then
            assertThat(result).isEqualTo("ReviewId{123}");
        }

        @Test
        @DisplayName("toString()은 ID 값을 포함한다")
        void toStringContainsValue() {
            // given
            ReviewId reviewId = ReviewId.of(999L);

            // when
            String result = reviewId.toString();

            // then
            assertThat(result).contains("999");
        }
    }
}
