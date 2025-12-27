package org.yyubin.application.auth.port;

/**
 * 토큰 블랙리스트 Port
 * JWT 토큰 무효화 기능을 제공합니다.
 */
public interface TokenBlacklistPort {

    /**
     * 토큰을 블랙리스트에 추가하여 무효화
     *
     * @param token JWT 토큰
     */
    void addToBlacklist(String token);

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true
     */
    boolean isBlacklisted(String token);

    /**
     * 특정 사용자의 모든 토큰 무효화
     *
     * @param userId 사용자 ID
     * @param maxTokenLifetimeSeconds 최대 토큰 유효 시간 (초)
     */
    void invalidateAllUserTokens(String userId, long maxTokenLifetimeSeconds);

    /**
     * 사용자의 모든 토큰이 무효화되었는지 확인
     *
     * @param userId 사용자 ID
     * @return 무효화되었으면 true
     */
    boolean isUserTokensInvalidated(String userId);
}
