package org.yyubin.domain.user;

/**
 * 비밀번호 정책 검증
 * - 최소 8자 이상
 * - 대문자 1개 이상
 * - 소문자 1개 이상
 * - 숫자 1개 이상
 * - 특수문자 1개 이상 (@$!%*?&)
 */
public class PasswordPolicy {

    private static final String PASSWORD_PATTERN =
        "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

    private PasswordPolicy() {
    }

    /**
     * 비밀번호 정책 검증
     *
     * @param password 검증할 비밀번호
     * @throws InvalidPasswordException 비밀번호가 정책을 만족하지 않을 경우
     */
    public static void validate(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidPasswordException("비밀번호는 필수입니다");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            throw new InvalidPasswordException(
                "비밀번호는 8자 이상, 대문자, 소문자, 숫자, 특수문자(@$!%*?&)를 각각 1개 이상 포함해야 합니다"
            );
        }
    }

    /**
     * 비밀번호가 정책을 만족하는지 확인 (예외를 던지지 않음)
     *
     * @param password 확인할 비밀번호
     * @return 정책을 만족하면 true, 아니면 false
     */
    public static boolean isValid(String password) {
        if (password == null || password.isBlank()) {
            return false;
        }
        return password.matches(PASSWORD_PATTERN);
    }
}
