package org.yyubin.api.auth.dto;

import org.yyubin.application.dto.AuthResult;

public record AuthResponse(
        Long userId,
        String email,
        String username
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.userId(), result.email(), result.username());
    }
}
