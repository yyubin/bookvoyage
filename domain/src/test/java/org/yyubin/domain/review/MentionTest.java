package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Mention 도메인 테스트")
class MentionTest {

    @Nested
    @DisplayName("Mention 생성")
    class CreateMention {

        @Test
        @DisplayName("유효한 데이터로 Mention을 생성할 수 있다")
        void createWithValidData() {
            // given
            Long mentionedUserId = 1L;
            int startIndex = 0;
            int endIndex = 10;

            // when
            Mention mention = new Mention(mentionedUserId, startIndex, endIndex);

            // then
            assertThat(mention).isNotNull();
            assertThat(mention.mentionedUserId()).isEqualTo(mentionedUserId);
            assertThat(mention.startIndex()).isEqualTo(startIndex);
            assertThat(mention.endIndex()).isEqualTo(endIndex);
        }

        @Test
        @DisplayName("startIndex와 endIndex가 같은 값으로 Mention을 생성할 수 있다")
        void createWithSameStartAndEndIndex() {
            // given
            Long mentionedUserId = 1L;
            int index = 5;

            // when
            Mention mention = new Mention(mentionedUserId, index, index);

            // then
            assertThat(mention).isNotNull();
            assertThat(mention.startIndex()).isEqualTo(index);
            assertThat(mention.endIndex()).isEqualTo(index);
        }

        @Test
        @DisplayName("startIndex가 0인 Mention을 생성할 수 있다")
        void createWithZeroStartIndex() {
            // given
            Long mentionedUserId = 1L;
            int startIndex = 0;
            int endIndex = 5;

            // when
            Mention mention = new Mention(mentionedUserId, startIndex, endIndex);

            // then
            assertThat(mention.startIndex()).isEqualTo(0);
        }

        @Test
        @DisplayName("큰 index 값으로 Mention을 생성할 수 있다")
        void createWithLargeIndices() {
            // given
            Long mentionedUserId = 1L;
            int startIndex = 1000;
            int endIndex = 2000;

            // when
            Mention mention = new Mention(mentionedUserId, startIndex, endIndex);

            // then
            assertThat(mention.startIndex()).isEqualTo(startIndex);
            assertThat(mention.endIndex()).isEqualTo(endIndex);
        }
    }

    @Nested
    @DisplayName("Mention 유효성 검증 - mentionedUserId")
    class ValidateMentionedUserId {

        @Test
        @DisplayName("null mentionedUserId로 생성 시 예외가 발생한다")
        void createWithNullMentionedUserId() {
            // when & then
            assertThatThrownBy(() -> new Mention(null, 0, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mentioned user id");
        }

        @Test
        @DisplayName("0 mentionedUserId로 생성 시 예외가 발생한다")
        void createWithZeroMentionedUserId() {
            // when & then
            assertThatThrownBy(() -> new Mention(0L, 0, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mentioned user id");
        }

        @ParameterizedTest
        @ValueSource(longs = {-1L, -10L, -100L, Long.MIN_VALUE})
        @DisplayName("음수 mentionedUserId로 생성 시 예외가 발생한다")
        void createWithNegativeMentionedUserId(Long negativeMentionedUserId) {
            // when & then
            assertThatThrownBy(() -> new Mention(negativeMentionedUserId, 0, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mentioned user id");
        }
    }

    @Nested
    @DisplayName("Mention 유효성 검증 - indices")
    class ValidateIndices {

        @Test
        @DisplayName("음수 startIndex로 생성 시 예외가 발생한다")
        void createWithNegativeStartIndex() {
            // when & then
            assertThatThrownBy(() -> new Mention(1L, -1, 10))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mention indices");
        }

        @Test
        @DisplayName("endIndex가 startIndex보다 작으면 예외가 발생한다")
        void createWithEndIndexLessThanStartIndex() {
            // when & then
            assertThatThrownBy(() -> new Mention(1L, 10, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mention indices");
        }

        @Test
        @DisplayName("음수 endIndex로 생성 시 예외가 발생한다")
        void createWithNegativeEndIndex() {
            // when & then
            assertThatThrownBy(() -> new Mention(1L, 0, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mention indices");
        }

        @Test
        @DisplayName("startIndex와 endIndex가 모두 음수이면 예외가 발생한다")
        void createWithBothNegativeIndices() {
            // when & then
            assertThatThrownBy(() -> new Mention(1L, -5, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid mention indices");
        }
    }

    @Nested
    @DisplayName("Mention 동등성")
    class MentionEquality {

        @Test
        @DisplayName("같은 값을 가진 Mention은 동등하다")
        void equalMentionsWithSameValues() {
            // given
            Mention mention1 = new Mention(1L, 0, 10);
            Mention mention2 = new Mention(1L, 0, 10);

            // when & then
            assertThat(mention1).isEqualTo(mention2);
            assertThat(mention1.hashCode()).isEqualTo(mention2.hashCode());
        }

        @Test
        @DisplayName("다른 mentionedUserId를 가진 Mention은 동등하지 않다")
        void notEqualMentionsWithDifferentUserId() {
            // given
            Mention mention1 = new Mention(1L, 0, 10);
            Mention mention2 = new Mention(2L, 0, 10);

            // when & then
            assertThat(mention1).isNotEqualTo(mention2);
        }

        @Test
        @DisplayName("다른 startIndex를 가진 Mention은 동등하지 않다")
        void notEqualMentionsWithDifferentStartIndex() {
            // given
            Mention mention1 = new Mention(1L, 0, 10);
            Mention mention2 = new Mention(1L, 5, 10);

            // when & then
            assertThat(mention1).isNotEqualTo(mention2);
        }

        @Test
        @DisplayName("다른 endIndex를 가진 Mention은 동등하지 않다")
        void notEqualMentionsWithDifferentEndIndex() {
            // given
            Mention mention1 = new Mention(1L, 0, 10);
            Mention mention2 = new Mention(1L, 0, 15);

            // when & then
            assertThat(mention1).isNotEqualTo(mention2);
        }

        @Test
        @DisplayName("Mention은 자기 자신과 동등하다")
        void equalToItself() {
            // given
            Mention mention = new Mention(1L, 0, 10);

            // when & then
            assertThat(mention).isEqualTo(mention);
        }

        @Test
        @DisplayName("Mention은 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            Mention mention = new Mention(1L, 0, 10);

            // when & then
            assertThat(mention).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("Mention toString")
    class MentionToString {

        @Test
        @DisplayName("toString()은 Mention 정보를 포함한 문자열을 반환한다")
        void toStringContainsMentionInfo() {
            // given
            Mention mention = new Mention(1L, 0, 10);

            // when
            String result = mention.toString();

            // then
            assertThat(result).contains("Mention");
            assertThat(result).contains("mentionedUserId=");
            assertThat(result).contains("startIndex=");
            assertThat(result).contains("endIndex=");
        }
    }

    @Nested
    @DisplayName("Mention 필드 접근")
    class MentionFieldAccess {

        @Test
        @DisplayName("모든 필드에 접근할 수 있다")
        void accessAllFields() {
            // given
            Long expectedUserId = 42L;
            int expectedStartIndex = 5;
            int expectedEndIndex = 15;
            Mention mention = new Mention(expectedUserId, expectedStartIndex, expectedEndIndex);

            // when & then
            assertThat(mention.mentionedUserId()).isEqualTo(expectedUserId);
            assertThat(mention.startIndex()).isEqualTo(expectedStartIndex);
            assertThat(mention.endIndex()).isEqualTo(expectedEndIndex);
        }
    }
}
