package org.yyubin.api.auth.dto;

import org.yyubin.application.dto.OAuth2TokenResult;

public record OAuth2TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static OAuth2TokenResponse from(OAuth2TokenResult result) {
        return new OAuth2TokenResponse(result.accessToken(), result.refreshToken());
    }
}
