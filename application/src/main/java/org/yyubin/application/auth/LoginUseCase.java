package org.yyubin.application.auth;

import org.yyubin.application.dto.AuthResult;

public interface LoginUseCase {
    AuthResult execute(String email, String password);
}
