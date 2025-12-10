package org.yyubin.application.auth;

import org.yyubin.application.dto.AuthResult;

public interface SignUpUseCase {
    AuthResult execute(String email, String password, String username, String bio);
}
