package org.yyubin.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("InvalidPasswordException 도메인 테스트")
class InvalidPasswordExceptionTest {

    @Nested
    @DisplayName("InvalidPasswordException 생성")
    class CreateInvalidPasswordException {

        @Test
        @DisplayName("메시지와 함께 InvalidPasswordException을 생성할 수 있다")
        void createWithMessage() {
            // given
            String message = "비밀번호가 유효하지 않습니다";

            // when
            InvalidPasswordException exception = new InvalidPasswordException(message);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("null 메시지로 InvalidPasswordException을 생성할 수 있다")
        void createWithNullMessage() {
            // when
            InvalidPasswordException exception = new InvalidPasswordException(null);

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isNull();
        }

        @Test
        @DisplayName("빈 문자열 메시지로 InvalidPasswordException을 생성할 수 있다")
        void createWithEmptyMessage() {
            // when
            InvalidPasswordException exception = new InvalidPasswordException("");

            // then
            assertThat(exception).isNotNull();
            assertThat(exception.getMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("InvalidPasswordException 계층 구조")
    class InvalidPasswordExceptionHierarchy {

        @Test
        @DisplayName("InvalidPasswordException은 IllegalArgumentException을 상속한다")
        void extendsIllegalArgumentException() {
            // given
            InvalidPasswordException exception = new InvalidPasswordException("test");

            // when & then
            assertThat(exception).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("InvalidPasswordException은 RuntimeException을 상속한다")
        void extendsRuntimeException() {
            // given
            InvalidPasswordException exception = new InvalidPasswordException("test");

            // when & then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("InvalidPasswordException은 Exception을 상속한다")
        void extendsException() {
            // given
            InvalidPasswordException exception = new InvalidPasswordException("test");

            // when & then
            assertThat(exception).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("InvalidPasswordException throw")
    class ThrowInvalidPasswordException {

        @Test
        @DisplayName("InvalidPasswordException을 던지고 받을 수 있다")
        void throwAndCatchException() {
            // given
            String message = "비밀번호가 너무 짧습니다";

            // when & then
            assertThatThrownBy(() -> {
                throw new InvalidPasswordException(message);
            })
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessage(message);
        }

        @Test
        @DisplayName("InvalidPasswordException을 IllegalArgumentException으로 받을 수 있다")
        void catchAsIllegalArgumentException() {
            // when & then
            assertThatThrownBy(() -> {
                throw new InvalidPasswordException("test");
            })
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("InvalidPasswordException을 RuntimeException으로 받을 수 있다")
        void catchAsRuntimeException() {
            // when & then
            assertThatThrownBy(() -> {
                throw new InvalidPasswordException("test");
            })
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("InvalidPasswordException 메시지")
    class InvalidPasswordExceptionMessage {

        @Test
        @DisplayName("예외 메시지에 비밀번호 정책 설명이 포함될 수 있다")
        void messageContainsPolicyDescription() {
            // given
            String message = "비밀번호는 8자 이상, 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함해야 합니다";

            // when
            InvalidPasswordException exception = new InvalidPasswordException(message);

            // then
            assertThat(exception.getMessage()).contains("8자 이상");
            assertThat(exception.getMessage()).contains("대문자");
            assertThat(exception.getMessage()).contains("소문자");
            assertThat(exception.getMessage()).contains("숫자");
            assertThat(exception.getMessage()).contains("특수문자");
        }

        @Test
        @DisplayName("예외 메시지가 정확히 전달된다")
        void messageIsPreserved() {
            // given
            String message = "비밀번호는 필수입니다";

            // when
            InvalidPasswordException exception = new InvalidPasswordException(message);

            // then
            assertThat(exception.getMessage()).isEqualTo(message);
        }
    }
}
