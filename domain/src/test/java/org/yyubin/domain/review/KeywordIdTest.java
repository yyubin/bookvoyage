package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("KeywordId 도메인 테스트")
class KeywordIdTest {

    @Nested
    @DisplayName("KeywordId 생성")
    class CreateKeywordId {

        @Test
        @DisplayName("유효한 양수 값으로 KeywordId를 생성할 수 있다")
        void createWithValidValue() {
            // given
            Long validValue = 1L;

            // when
            KeywordId keywordId = new KeywordId(validValue);

            // then
            assertThat(keywordId).isNotNull();
            assertThat(keywordId.value()).isEqualTo(validValue);
        }

        @Test
        @DisplayName("큰 숫자의 양수 값으로 KeywordId를 생성할 수 있다")
        void createWithLargeValue() {
            // given
            Long largeValue = Long.MAX_VALUE;

            // when
            KeywordId keywordId = new KeywordId(largeValue);

            // then
            assertThat(keywordId).isNotNull();
            assertThat(keywordId.value()).isEqualTo(largeValue);
        }

        @Test
        @DisplayName("null 값으로 KeywordId 생성 시 예외가 발생한다")
        void createWithNullValue() {
            // when & then
            assertThatThrownBy(() -> new KeywordId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid keyword ID");
        }

        @Test
        @DisplayName("0으로 KeywordId 생성 시 예외가 발생한다")
        void createWithZeroValue() {
            // when & then
            assertThatThrownBy(() -> new KeywordId(0L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid keyword ID");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -10L, -100L, Long.MIN_VALUE})
        @DisplayName("음수 값으로 KeywordId 생성 시 예외가 발생한다")
        void createWithNegativeValue(Long negativeValue) {
            // when & then
            assertThatThrownBy(() -> new KeywordId(negativeValue))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid keyword ID");
        }
    }

    @Nested
    @DisplayName("KeywordId 동등성")
    class KeywordIdEquality {

        @Test
        @DisplayName("같은 값을 가진 KeywordId는 동등하다")
        void equalKeywordIdsWithSameValue() {
            // given
            KeywordId keywordId1 = new KeywordId(1L);
            KeywordId keywordId2 = new KeywordId(1L);

            // when & then
            assertThat(keywordId1).isEqualTo(keywordId2);
            assertThat(keywordId1.hashCode()).isEqualTo(keywordId2.hashCode());
        }

        @Test
        @DisplayName("다른 값을 가진 KeywordId는 동등하지 않다")
        void notEqualKeywordIdsWithDifferentValue() {
            // given
            KeywordId keywordId1 = new KeywordId(1L);
            KeywordId keywordId2 = new KeywordId(2L);

            // when & then
            assertThat(keywordId1).isNotEqualTo(keywordId2);
        }

        @Test
        @DisplayName("KeywordId는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            KeywordId keywordId = new KeywordId(1L);

            // when & then
            assertThat(keywordId).isEqualTo(keywordId);
        }

        @Test
        @DisplayName("KeywordId는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            KeywordId keywordId = new KeywordId(1L);

            // when & then
            assertThat(keywordId).isNotEqualTo(null);
        }

        @Test
        @DisplayName("KeywordId는 다른 타입의 객체와 동등하지 않다")
        void notEqualToDifferentType() {
            // given
            KeywordId keywordId = new KeywordId(1L);
            Long differentType = 1L;

            // when & then
            assertThat(keywordId).isNotEqualTo(differentType);
        }
    }

    @Nested
    @DisplayName("KeywordId toString")
    class KeywordIdToString {

        @Test
        @DisplayName("toString()은 KeywordId 정보를 포함한 문자열을 반환한다")
        void toStringContainsKeywordIdInfo() {
            // given
            KeywordId keywordId = new KeywordId(123L);

            // when
            String result = keywordId.toString();

            // then
            assertThat(result).contains("KeywordId");
            assertThat(result).contains("123");
        }
    }

    @Nested
    @DisplayName("KeywordId value 접근")
    class KeywordIdValueAccess {

        @Test
        @DisplayName("value() 메서드로 값에 접근할 수 있다")
        void accessValueMethod() {
            // given
            Long expectedValue = 42L;
            KeywordId keywordId = new KeywordId(expectedValue);

            // when
            Long actualValue = keywordId.value();

            // then
            assertThat(actualValue).isEqualTo(expectedValue);
        }
    }
}
