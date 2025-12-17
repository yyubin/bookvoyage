package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewVisibility 도메인 테스트")
class ReviewVisibilityTest {

    @Nested
    @DisplayName("ReviewVisibility Enum 값")
    class ReviewVisibilityValues {

        @Test
        @DisplayName("PUBLIC 값이 존재한다")
        void publicValueExists() {
            // when
            ReviewVisibility visibility = ReviewVisibility.PUBLIC;

            // then
            assertThat(visibility).isNotNull();
            assertThat(visibility.name()).isEqualTo("PUBLIC");
        }

        @Test
        @DisplayName("PRIVATE 값이 존재한다")
        void privateValueExists() {
            // when
            ReviewVisibility visibility = ReviewVisibility.PRIVATE;

            // then
            assertThat(visibility).isNotNull();
            assertThat(visibility.name()).isEqualTo("PRIVATE");
        }

        @Test
        @DisplayName("모든 ReviewVisibility 값은 2개이다")
        void allValuesCount() {
            // when
            ReviewVisibility[] values = ReviewVisibility.values();

            // then
            assertThat(values).hasSize(2);
        }
    }

    @Nested
    @DisplayName("ReviewVisibility from 메서드")
    class ReviewVisibilityFrom {

        @Test
        @DisplayName("'public' 문자열로 PUBLIC을 생성할 수 있다")
        void createPublicFromLowercase() {
            // when
            ReviewVisibility visibility = ReviewVisibility.from("public");

            // then
            assertThat(visibility).isEqualTo(ReviewVisibility.PUBLIC);
        }

        @Test
        @DisplayName("'PUBLIC' 문자열로 PUBLIC을 생성할 수 있다")
        void createPublicFromUppercase() {
            // when
            ReviewVisibility visibility = ReviewVisibility.from("PUBLIC");

            // then
            assertThat(visibility).isEqualTo(ReviewVisibility.PUBLIC);
        }

        @Test
        @DisplayName("'private' 문자열로 PRIVATE을 생성할 수 있다")
        void createPrivateFromLowercase() {
            // when
            ReviewVisibility visibility = ReviewVisibility.from("private");

            // then
            assertThat(visibility).isEqualTo(ReviewVisibility.PRIVATE);
        }

        @Test
        @DisplayName("'PRIVATE' 문자열로 PRIVATE을 생성할 수 있다")
        void createPrivateFromUppercase() {
            // when
            ReviewVisibility visibility = ReviewVisibility.from("PRIVATE");

            // then
            assertThat(visibility).isEqualTo(ReviewVisibility.PRIVATE);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백 문자열은 PUBLIC을 반환한다")
        void nullOrBlankReturnsPublic(String blankValue) {
            // when
            ReviewVisibility visibility = ReviewVisibility.from(blankValue);

            // then
            assertThat(visibility).isEqualTo(ReviewVisibility.PUBLIC);
        }

        @Test
        @DisplayName("대소문자 혼합 문자열로도 생성할 수 있다")
        void createWithMixedCase() {
            // when
            ReviewVisibility visibility1 = ReviewVisibility.from("PuBlIc");
            ReviewVisibility visibility2 = ReviewVisibility.from("pRiVaTe");

            // then
            assertThat(visibility1).isEqualTo(ReviewVisibility.PUBLIC);
            assertThat(visibility2).isEqualTo(ReviewVisibility.PRIVATE);
        }

        @Test
        @DisplayName("유효하지 않은 값으로 생성 시 예외가 발생한다")
        void createWithInvalidValue() {
            // when & then
            assertThatThrownBy(() -> ReviewVisibility.from("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid review visibility: INVALID");
        }
    }

    @Nested
    @DisplayName("ReviewVisibility isPublic 메서드")
    class ReviewVisibilityIsPublic {

        @Test
        @DisplayName("PUBLIC은 isPublic()이 true를 반환한다")
        void publicIsPublic() {
            // given
            ReviewVisibility visibility = ReviewVisibility.PUBLIC;

            // when & then
            assertThat(visibility.isPublic()).isTrue();
        }

        @Test
        @DisplayName("PRIVATE은 isPublic()이 false를 반환한다")
        void privateIsNotPublic() {
            // given
            ReviewVisibility visibility = ReviewVisibility.PRIVATE;

            // when & then
            assertThat(visibility.isPublic()).isFalse();
        }
    }

    @Nested
    @DisplayName("ReviewVisibility 동등성")
    class ReviewVisibilityEquality {

        @Test
        @DisplayName("같은 값의 ReviewVisibility는 동등하다")
        void sameValuesAreEqual() {
            // given
            ReviewVisibility visibility1 = ReviewVisibility.PUBLIC;
            ReviewVisibility visibility2 = ReviewVisibility.PUBLIC;

            // when & then
            assertThat(visibility1).isEqualTo(visibility2);
            assertThat(visibility1).isSameAs(visibility2);
        }

        @Test
        @DisplayName("다른 값의 ReviewVisibility는 동등하지 않다")
        void differentValuesAreNotEqual() {
            // given
            ReviewVisibility visibility1 = ReviewVisibility.PUBLIC;
            ReviewVisibility visibility2 = ReviewVisibility.PRIVATE;

            // when & then
            assertThat(visibility1).isNotEqualTo(visibility2);
        }
    }
}
