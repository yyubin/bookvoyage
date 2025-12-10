package org.yyubin.api.auth.dto;

import org.yyubin.application.dto.AuthResult;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long userId,
        String email,
        String username
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken(), result.userId(), result.email(), result.username());
    }
}
