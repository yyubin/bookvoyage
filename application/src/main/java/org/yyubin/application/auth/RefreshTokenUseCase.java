package org.yyubin.application.auth;

import org.yyubin.application.dto.AuthResult;

public interface RefreshTokenUseCase {
    AuthResult execute(String refreshToken);
}
