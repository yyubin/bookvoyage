package org.yyubin.application.auth.port;

import java.util.Optional;

/**
 * OAuth2 일회용 코드 저장/조회를 위한 포트
 */
public interface OAuth2CodePort {

    /**
     * 일회용 코드와 토큰 데이터 저장
     * @param code 일회용 코드
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     */
    void saveCode(String code, String accessToken, String refreshToken);

    /**
     * 코드로 토큰 데이터 조회 및 삭제 (일회용)
     * @param code 일회용 코드
     * @return 토큰 데이터 (accessToken:refreshToken)
     */
    Optional<String> getAndDeleteCode(String code);
}
