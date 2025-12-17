package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewReactionId 도메인 테스트")
class ReviewReactionIdTest {

    @Nested
    @DisplayName("ReviewReactionId 생성")
    class CreateReviewReactionId {

        @Test
        @DisplayName("유효한 양수 ID로 ReviewReactionId를 생성할 수 있다")
        void createWithValidId() {
            // given
            Long validId = 1L;

            // when
            ReviewReactionId reactionId = ReviewReactionId.of(validId);

            // then
            assertThat(reactionId).isNotNull();
            assertThat(reactionId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("큰 숫자의 양수 ID로 ReviewReactionId를 생성할 수 있다")
        void createWithLargeId() {
            // given
            Long largeId = Long.MAX_VALUE;

            // when
            ReviewReactionId reactionId = ReviewReactionId.of(largeId);

            // then
            assertThat(reactionId).isNotNull();
            assertThat(reactionId.getValue()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("null ID로 ReviewReactionId 생성 시 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> ReviewReactionId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reaction ID must be positive");
        }

        @Test
        @DisplayName("0으로 ReviewReactionId 생성 시 예외가 발생한다")
        void createWithZeroId() {
            // when & then
            assertThatThrownBy(() -> ReviewReactionId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reaction ID must be positive");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -10L, -100L, Long.MIN_VALUE})
        @DisplayName("음수 ID로 ReviewReactionId 생성 시 예외가 발생한다")
        void createWithNegativeId(Long negativeId) {
            // when & then
            assertThatThrownBy(() -> ReviewReactionId.of(negativeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Reaction ID must be positive");
        }
    }

    @Nested
    @DisplayName("ReviewReactionId 동등성")
    class ReviewReactionIdEquality {

        @Test
        @DisplayName("같은 값을 가진 ReviewReactionId는 동등하다")
        void equalReactionIdsWithSameValue() {
            // given
            ReviewReactionId reactionId1 = ReviewReactionId.of(1L);
            ReviewReactionId reactionId2 = ReviewReactionId.of(1L);

            // when & then
            assertThat(reactionId1).isEqualTo(reactionId2);
            assertThat(reactionId1.hashCode()).isEqualTo(reactionId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ReviewReactionId는 동등하지 않다")
        void notEqualReactionIdsWithDifferentValue() {
            // given
            ReviewReactionId reactionId1 = ReviewReactionId.of(1L);
            ReviewReactionId reactionId2 = ReviewReactionId.of(2L);

            // when & then
            assertThat(reactionId1).isNotEqualTo(reactionId2);
        }

        @Test
        @DisplayName("ReviewReactionId는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            ReviewReactionId reactionId = ReviewReactionId.of(1L);

            // when & then
            assertThat(reactionId).isEqualTo(reactionId);
        }

        @Test
        @DisplayName("ReviewReactionId는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            ReviewReactionId reactionId = ReviewReactionId.of(1L);

            // when & then
            assertThat(reactionId).isNotEqualTo(null);
        }

        @Test
        @DisplayName("ReviewReactionId는 다른 타입의 객체와 동등하지 않다")
        void notEqualToDifferentType() {
            // given
            ReviewReactionId reactionId = ReviewReactionId.of(1L);
            String differentType = "1";

            // when & then
            assertThat(reactionId).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("ReviewReactionId toString")
    class ReviewReactionIdToString {

        @Test
        @DisplayName("toString()은 올바른 형식의 문자열을 반환한다")
        void toStringFormat() {
            // given
            ReviewReactionId reactionId = ReviewReactionId.of(123L);

            // when
            String result = reactionId.toString();

            // then
            assertThat(result).isEqualTo("ReviewReactionId{123}");
        }

        @Test
        @DisplayName("toString()은 ID 값을 포함한다")
        void toStringContainsValue() {
            // given
            ReviewReactionId reactionId = ReviewReactionId.of(999L);

            // when
            String result = reactionId.toString();

            // then
            assertThat(result).contains("999");
        }
    }
}
