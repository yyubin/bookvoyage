package org.yyubin.application.auth;

/**
 * 로그아웃 Use Case
 * 토큰을 무효화하고 사용자 세션을 종료합니다.
 */
public interface LogoutUseCase {

    /**
     * 로그아웃 처리
     *
     * @param accessToken Access Token (nullable)
     * @param refreshToken Refresh Token (nullable)
     */
    void execute(String accessToken, String refreshToken);
}
