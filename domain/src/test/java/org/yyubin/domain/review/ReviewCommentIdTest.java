package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewCommentId 도메인 테스트")
class ReviewCommentIdTest {

    @Nested
    @DisplayName("ReviewCommentId 생성")
    class CreateReviewCommentId {

        @Test
        @DisplayName("유효한 양수 ID로 ReviewCommentId를 생성할 수 있다")
        void createWithValidId() {
            // given
            Long validId = 1L;

            // when
            ReviewCommentId commentId = ReviewCommentId.of(validId);

            // then
            assertThat(commentId).isNotNull();
            assertThat(commentId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("큰 숫자의 양수 ID로 ReviewCommentId를 생성할 수 있다")
        void createWithLargeId() {
            // given
            Long largeId = Long.MAX_VALUE;

            // when
            ReviewCommentId commentId = ReviewCommentId.of(largeId);

            // then
            assertThat(commentId).isNotNull();
            assertThat(commentId.getValue()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("null ID로 ReviewCommentId 생성 시 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> ReviewCommentId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment ID must be positive");
        }

        @Test
        @DisplayName("0으로 ReviewCommentId 생성 시 예외가 발생한다")
        void createWithZeroId() {
            // when & then
            assertThatThrownBy(() -> ReviewCommentId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment ID must be positive");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -10L, -100L, Long.MIN_VALUE})
        @DisplayName("음수 ID로 ReviewCommentId 생성 시 예외가 발생한다")
        void createWithNegativeId(Long negativeId) {
            // when & then
            assertThatThrownBy(() -> ReviewCommentId.of(negativeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Comment ID must be positive");
        }
    }

    @Nested
    @DisplayName("ReviewCommentId 동등성")
    class ReviewCommentIdEquality {

        @Test
        @DisplayName("같은 값을 가진 ReviewCommentId는 동등하다")
        void equalCommentIdsWithSameValue() {
            // given
            ReviewCommentId commentId1 = ReviewCommentId.of(1L);
            ReviewCommentId commentId2 = ReviewCommentId.of(1L);

            // when & then
            assertThat(commentId1).isEqualTo(commentId2);
            assertThat(commentId1.hashCode()).isEqualTo(commentId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 ReviewCommentId는 동등하지 않다")
        void notEqualCommentIdsWithDifferentValue() {
            // given
            ReviewCommentId commentId1 = ReviewCommentId.of(1L);
            ReviewCommentId commentId2 = ReviewCommentId.of(2L);

            // when & then
            assertThat(commentId1).isNotEqualTo(commentId2);
        }

        @Test
        @DisplayName("ReviewCommentId는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            ReviewCommentId commentId = ReviewCommentId.of(1L);

            // when & then
            assertThat(commentId).isEqualTo(commentId);
        }

        @Test
        @DisplayName("ReviewCommentId는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            ReviewCommentId commentId = ReviewCommentId.of(1L);

            // when & then
            assertThat(commentId).isNotEqualTo(null);
        }

        @Test
        @DisplayName("ReviewCommentId는 다른 타입의 객체와 동등하지 않다")
        void notEqualToDifferentType() {
            // given
            ReviewCommentId commentId = ReviewCommentId.of(1L);
            String differentType = "1";

            // when & then
            assertThat(commentId).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("ReviewCommentId toString")
    class ReviewCommentIdToString {

        @Test
        @DisplayName("toString()은 올바른 형식의 문자열을 반환한다")
        void toStringFormat() {
            // given
            ReviewCommentId commentId = ReviewCommentId.of(123L);

            // when
            String result = commentId.toString();

            // then
            assertThat(result).isEqualTo("ReviewCommentId{123}");
        }

        @Test
        @DisplayName("toString()은 ID 값을 포함한다")
        void toStringContainsValue() {
            // given
            ReviewCommentId commentId = ReviewCommentId.of(999L);

            // when
            String result = commentId.toString();

            // then
            assertThat(result).contains("999");
        }
    }
}
