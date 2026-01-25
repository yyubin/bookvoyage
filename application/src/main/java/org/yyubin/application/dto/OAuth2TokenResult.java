package org.yyubin.application.dto;

public record OAuth2TokenResult(
        String accessToken,
        String refreshToken
) {
    public OAuth2TokenResult {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token cannot be null or blank");
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token cannot be null or blank");
        }
    }
}
