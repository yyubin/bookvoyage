package org.yyubin.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PasswordPolicy 도메인 테스트")
class PasswordPolicyTest {

    @Nested
    @DisplayName("PasswordPolicy validate 메서드 - 유효한 비밀번호")
    class ValidateValidPasswords {

        @Test
        @DisplayName("모든 조건을 만족하는 비밀번호는 검증을 통과한다")
        void validateValidPassword() {
            // given
            String validPassword = "Password1!";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(validPassword))
                    .doesNotThrowAnyException();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password1!",
                "Test1234!",
                "Abcd123@",
                "MyP@ssw0rd",
                "Secure1$",
                "Strong9&",
                "Valid8*A",
                "Good7%Bc",
                "Nice6?Cd",
                "P@ssw0rd123"
        })
        @DisplayName("정책을 만족하는 다양한 비밀번호는 검증을 통과한다")
        void validateVariousValidPasswords(String password) {
            // when & then
            assertThatCode(() -> PasswordPolicy.validate(password))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("최소 8자의 유효한 비밀번호는 검증을 통과한다")
        void validateMinimumLengthPassword() {
            // given
            String password = "Abcd123!";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(password))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("긴 비밀번호도 검증을 통과한다")
        void validateLongPassword() {
            // given
            String password = "VeryLongPassword123!@$%";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(password))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("PasswordPolicy validate 메서드 - 유효하지 않은 비밀번호")
    class ValidateInvalidPasswords {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백만 있는 비밀번호는 예외를 발생시킨다")
        void validateBlankPassword(String blankPassword) {
            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(blankPassword))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("비밀번호는 필수입니다");
        }

        @Test
        @DisplayName("8자 미만의 비밀번호는 예외를 발생시킨다")
        void validateTooShortPassword() {
            // given
            String shortPassword = "Pass1!";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(shortPassword))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("8자 이상");
        }

        @Test
        @DisplayName("대문자가 없는 비밀번호는 예외를 발생시킨다")
        void validateNoUppercasePassword() {
            // given
            String password = "password1!";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(password))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("대문자");
        }

        @Test
        @DisplayName("소문자가 없는 비밀번호는 예외를 발생시킨다")
        void validateNoLowercasePassword() {
            // given
            String password = "PASSWORD1!";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(password))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("소문자");
        }

        @Test
        @DisplayName("숫자가 없는 비밀번호는 예외를 발생시킨다")
        void validateNoDigitPassword() {
            // given
            String password = "Password!";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(password))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("숫자");
        }

        @Test
        @DisplayName("특수문자가 없는 비밀번호는 예외를 발생시킨다")
        void validateNoSpecialCharPassword() {
            // given
            String password = "Password1";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(password))
                    .isInstanceOf(InvalidPasswordException.class)
                    .hasMessageContaining("특수문자");
        }

        @Test
        @DisplayName("허용되지 않은 특수문자를 포함한 비밀번호는 예외를 발생시킨다")
        void validateInvalidSpecialCharPassword() {
            // given
            String password = "Password1#";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(password))
                    .isInstanceOf(InvalidPasswordException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "password",
                "12345678",
                "PASSWORD",
                "Pass word1!",
                "한글비밀번호1!",
                "Password1",
                "password1!",
                "PASSWORD1!"
        })
        @DisplayName("정책을 만족하지 않는 다양한 비밀번호는 예외를 발생시킨다")
        void validateVariousInvalidPasswords(String password) {
            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(password))
                    .isInstanceOf(InvalidPasswordException.class);
        }
    }

    @Nested
    @DisplayName("PasswordPolicy isValid 메서드 - 유효한 비밀번호")
    class IsValidValidPasswords {

        @Test
        @DisplayName("모든 조건을 만족하는 비밀번호는 true를 반환한다")
        void isValidReturnsTrueForValidPassword() {
            // given
            String validPassword = "Password1!";

            // when
            boolean result = PasswordPolicy.isValid(validPassword);

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Password1!",
                "Test1234!",
                "Abcd123@",
                "MyP@ssw0rd",
                "Secure1$",
                "Strong9&",
                "Valid8*A",
                "P@ssw0rd123"
        })
        @DisplayName("정책을 만족하는 다양한 비밀번호는 true를 반환한다")
        void isValidReturnsTrueForVariousValidPasswords(String password) {
            // when
            boolean result = PasswordPolicy.isValid(password);

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("PasswordPolicy isValid 메서드 - 유효하지 않은 비밀번호")
    class IsValidInvalidPasswords {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백만 있는 비밀번호는 false를 반환한다")
        void isValidReturnsFalseForBlankPassword(String blankPassword) {
            // when
            boolean result = PasswordPolicy.isValid(blankPassword);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "Pass1!",
                "password1!",
                "PASSWORD1!",
                "Password!",
                "Password1",
                "password",
                "12345678",
                "Password1#"
        })
        @DisplayName("정책을 만족하지 않는 비밀번호는 false를 반환한다")
        void isValidReturnsFalseForInvalidPassword(String password) {
            // when
            boolean result = PasswordPolicy.isValid(password);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("8자 미만의 비밀번호는 false를 반환한다")
        void isValidReturnsFalseForTooShortPassword() {
            // given
            String shortPassword = "Pass1!";

            // when
            boolean result = PasswordPolicy.isValid(shortPassword);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("PasswordPolicy validate vs isValid")
    class ValidateVsIsValid {

        @Test
        @DisplayName("유효한 비밀번호에 대해 validate는 예외를 발생시키지 않고 isValid는 true를 반환한다")
        void validPasswordBehavior() {
            // given
            String validPassword = "Password1!";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(validPassword))
                    .doesNotThrowAnyException();
            assertThat(PasswordPolicy.isValid(validPassword)).isTrue();
        }

        @Test
        @DisplayName("유효하지 않은 비밀번호에 대해 validate는 예외를 발생시키고 isValid는 false를 반환한다")
        void invalidPasswordBehavior() {
            // given
            String invalidPassword = "password";

            // when & then
            assertThatThrownBy(() -> PasswordPolicy.validate(invalidPassword))
                    .isInstanceOf(InvalidPasswordException.class);
            assertThat(PasswordPolicy.isValid(invalidPassword)).isFalse();
        }
    }

    @Nested
    @DisplayName("PasswordPolicy 특수 케이스")
    class PasswordPolicySpecialCases {

        @Test
        @DisplayName("허용된 모든 특수문자를 포함한 비밀번호는 검증을 통과한다")
        void validateAllAllowedSpecialChars() {
            // given
            String password = "Password1@$!%*?&";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(password))
                    .doesNotThrowAnyException();
            assertThat(PasswordPolicy.isValid(password)).isTrue();
        }

        @Test
        @DisplayName("정확히 8자의 비밀번호는 검증을 통과한다")
        void validateExactly8CharsPassword() {
            // given
            String password = "Abcd123!";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(password))
                    .doesNotThrowAnyException();
            assertThat(PasswordPolicy.isValid(password)).isTrue();
        }

        @Test
        @DisplayName("여러 개의 대문자, 소문자, 숫자, 특수문자를 포함한 비밀번호는 검증을 통과한다")
        void validateMultipleRequiredChars() {
            // given
            String password = "ABCDabcd1234!@$%";

            // when & then
            assertThatCode(() -> PasswordPolicy.validate(password))
                    .doesNotThrowAnyException();
            assertThat(PasswordPolicy.isValid(password)).isTrue();
        }
    }
}
