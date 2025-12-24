package org.yyubin.api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
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
import org.yyubin.support.jwt.JwtProperties;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final JwtProperties jwtProperties;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(
            @Valid @RequestBody SignUpRequest request,
            HttpServletResponse response) {

        AuthResult result = signUpUseCase.execute(
                request.email(),
                request.password(),
                request.username(),
                request.bio()
        );

        setTokenCookies(response, result.accessToken(), result.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthResponse.from(result));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        log.info("Login request for email: {}", request.email());

        AuthResult authResult = loginUseCase.execute(request.email(), request.password());
        setTokenCookies(response, authResult.accessToken(), authResult.refreshToken());

        return ResponseEntity.ok(AuthResponse.from(authResult));
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token 쿠키 설정
        Cookie accessTokenCookie = createCookie(
                "accessToken",
                accessToken,
                (int) (jwtProperties.getAccessTokenExpiration() / 1000)
        );
        response.addCookie(accessTokenCookie);

        // Refresh Token 쿠키 설정
        Cookie refreshTokenCookie = createCookie(
                "refreshToken",
                refreshToken,
                (int) (jwtProperties.getRefreshTokenExpiration() / 1000)
        );
        response.addCookie(refreshTokenCookie);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        // SameSite 속성은 Spring Boot 3.x부터 ResponseCookie 또는 헤더를 통해 설정 가능
        return cookie;
    }
}
