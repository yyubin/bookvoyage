package org.yyubin.api.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.auth.dto.AuthResponse;
import org.yyubin.api.auth.dto.OAuth2TokenResponse;
import org.yyubin.api.auth.dto.LoginRequest;
import org.yyubin.api.auth.dto.SignUpRequest;
import org.yyubin.application.auth.LoginUseCase;
import org.yyubin.application.auth.LogoutUseCase;
import org.yyubin.application.auth.OAuth2CodeExchangeUseCase;
import org.yyubin.application.auth.RefreshTokenUseCase;
import org.yyubin.application.auth.SignUpUseCase;
import org.yyubin.application.dto.AuthResult;
import org.yyubin.application.dto.OAuth2TokenResult;
import org.yyubin.support.jwt.JwtProperties;
import org.yyubin.support.web.CookieProperties;

import java.util.Arrays;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final OAuth2CodeExchangeUseCase oAuth2CodeExchangeUseCase;
    private final LogoutCleanupHandler logoutCleanupHandler;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;

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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout request");

        // 1. 토큰 추출
        String accessToken = extractTokenFromRequest(request, "accessToken");
        String refreshToken = extractTokenFromRequest(request, "refreshToken");

        // 2. 로그아웃 처리 (토큰 블랙리스트 추가)
        logoutUseCase.execute(accessToken, refreshToken);

        // 3. 클라이언트/세션 정리
        logoutCleanupHandler.clear(request, response);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        log.info("Refresh token request");

        String refreshToken = extractTokenFromRequest(request, "refreshToken");
        if (!StringUtils.hasText(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthResult authResult = refreshTokenUseCase.execute(refreshToken);
        setTokenCookies(response, authResult.accessToken(), authResult.refreshToken());

        return ResponseEntity.ok(AuthResponse.from(authResult));
    }

    /**
     * OAuth2 일회용 코드로 토큰 교환
     * 프론트엔드에서 fetch로 호출하면 쿠키가 정상적으로 설정됨
     */
    @PostMapping("/oauth2/token")
    public ResponseEntity<OAuth2TokenResponse> exchangeOAuth2Code(
            @RequestParam String code,
            HttpServletResponse response) {
        log.info("OAuth2 code exchange request, code: {}", code);

        try {
            OAuth2TokenResult tokenResult = oAuth2CodeExchangeUseCase.execute(code);
            log.info("UseCase returned: accessToken={}, refreshToken={}",
                    tokenResult.accessToken() != null ? "present" : "null",
                    tokenResult.refreshToken() != null ? "present" : "null");

            // 쿠키 설정 (이메일 로그인과 동일!)
            setTokenCookies(response, tokenResult.accessToken(), tokenResult.refreshToken());

            log.info("OAuth2 code exchange success, cookies set");

            return ResponseEntity.ok(OAuth2TokenResponse.from(tokenResult));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid or expired OAuth2 code: {}, error: {}", code, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Unexpected error during OAuth2 code exchange: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Access Token 쿠키 설정
        addCookie(
                response,
                "accessToken",
                accessToken,
                (int) (jwtProperties.getAccessTokenExpiration() / 1000)
        );

        // Refresh Token 쿠키 설정
        addCookie(
                response,
                "refreshToken",
                refreshToken,
                (int) (jwtProperties.getRefreshTokenExpiration() / 1000)
        );
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path(cookieProperties.getPath())
                .maxAge(maxAge);

        if (cookieProperties.getSameSite() != null && !cookieProperties.getSameSite().isBlank()) {
            builder.sameSite(cookieProperties.getSameSite());
        }
        if (cookieProperties.getDomain() != null && !cookieProperties.getDomain().isBlank()) {
            builder.domain(cookieProperties.getDomain());
        }

        response.addHeader("Set-Cookie", builder.build().toString());
    }

    private String extractTokenFromRequest(HttpServletRequest request, String cookieName) {
        // 1. Authorization 헤더에서 추출 (accessToken만 해당)
        if ("accessToken".equals(cookieName)) {
            String bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
        }

        // 2. 쿠키에서 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
