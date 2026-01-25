package org.yyubin.application.auth;

import org.yyubin.application.dto.OAuth2TokenResult;

/**
 * OAuth2 일회용 코드를 토큰으로 교환하는 유스케이스
 */
public interface OAuth2CodeExchangeUseCase {

    /**
     * 일회용 코드로 토큰 교환
     * @param code 일회용 코드
     * @return 인증 결과 (토큰 포함)
     */
    OAuth2TokenResult execute(String code);
}
