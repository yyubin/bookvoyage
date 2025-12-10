package org.yyubin.api.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.auth.dto.AuthResponse;
import org.yyubin.api.auth.dto.LoginRequest;
import org.yyubin.api.auth.dto.SignUpRequest;
import org.yyubin.application.auth.LoginUseCase;
import org.yyubin.application.auth.SignUpUseCase;
import org.yyubin.application.dto.AuthResult;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(
            @Valid @RequestBody SignUpRequest request) {

        AuthResult result = signUpUseCase.execute(
                request.email(),
                request.password(),
                request.username(),
                request.bio()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.from(result));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());

        AuthResult authResult = loginUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(AuthResponse.from(authResult));
    }
}
