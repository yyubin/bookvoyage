package org.yyubin.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookId 도메인 테스트")
class BookIdTest {

    @Nested
    @DisplayName("BookId 생성")
    class CreateBookId {

        @Test
        @DisplayName("유효한 양수 ID로 BookId를 생성할 수 있다")
        void createWithValidId() {
            // given
            Long validId = 1L;

            // when
            BookId bookId = BookId.of(validId);

            // then
            assertThat(bookId).isNotNull();
            assertThat(bookId.getValue()).isEqualTo(validId);
        }

        @Test
        @DisplayName("큰 숫자의 양수 ID로 BookId를 생성할 수 있다")
        void createWithLargeId() {
            // given
            Long largeId = Long.MAX_VALUE;

            // when
            BookId bookId = BookId.of(largeId);

            // then
            assertThat(bookId).isNotNull();
            assertThat(bookId.getValue()).isEqualTo(largeId);
        }

        @Test
        @DisplayName("null ID로 BookId 생성 시 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> BookId.of(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book ID must be positive");
        }

        @Test
        @DisplayName("0으로 BookId 생성 시 예외가 발생한다")
        void createWithZeroId() {
            // when & then
            assertThatThrownBy(() -> BookId.of(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book ID must be positive");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -10L, -100L, Long.MIN_VALUE})
        @DisplayName("음수 ID로 BookId 생성 시 예외가 발생한다")
        void createWithNegativeId(Long negativeId) {
            // when & then
            assertThatThrownBy(() -> BookId.of(negativeId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book ID must be positive");
        }
    }

    @Nested
    @DisplayName("BookId 동등성")
    class BookIdEquality {

        @Test
        @DisplayName("같은 값을 가진 BookId는 동등하다")
        void equalBookIdsWithSameValue() {
            // given
            BookId bookId1 = BookId.of(1L);
            BookId bookId2 = BookId.of(1L);

            // when & then
            assertThat(bookId1).isEqualTo(bookId2);
            assertThat(bookId1.hashCode()).isEqualTo(bookId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 BookId는 동등하지 않다")
        void notEqualBookIdsWithDifferentValue() {
            // given
            BookId bookId1 = BookId.of(1L);
            BookId bookId2 = BookId.of(2L);

            // when & then
            assertThat(bookId1).isNotEqualTo(bookId2);
        }

        @Test
        @DisplayName("BookId는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            BookId bookId = BookId.of(1L);

            // when & then
            assertThat(bookId).isEqualTo(bookId);
        }

        @Test
        @DisplayName("BookId는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            BookId bookId = BookId.of(1L);

            // when & then
            assertThat(bookId).isNotEqualTo(null);
        }

        @Test
        @DisplayName("BookId는 다른 타입의 객체와 동등하지 않다")
        void notEqualToDifferentType() {
            // given
            BookId bookId = BookId.of(1L);
            String differentType = "1";

            // when & then
            assertThat(bookId).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("BookId toString")
    class BookIdToString {

        @Test
        @DisplayName("toString()은 올바른 형식의 문자열을 반환한다")
        void toStringFormat() {
            // given
            BookId bookId = BookId.of(123L);

            // when
            String result = bookId.toString();

            // then
            assertThat(result).isEqualTo("BookId{123}");
        }

        @Test
        @DisplayName("toString()은 ID 값을 포함한다")
        void toStringContainsValue() {
            // given
            BookId bookId = BookId.of(999L);

            // when
            String result = bookId.toString();

            // then
            assertThat(result).contains("999");
        }
    }
}
