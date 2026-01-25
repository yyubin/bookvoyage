package org.yyubin.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.yyubin.application.auth.port.OAuth2CodePort;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;
import org.yyubin.support.jwt.JwtProperties;
import org.yyubin.support.jwt.JwtProvider;
import org.yyubin.support.web.CookieProperties;
import org.yyubin.support.web.FrontendProperties;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;
    private final FrontendProperties frontendProperties;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final OAuth2CodePort oAuth2CodePort;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        // OAuth2 인증 쿠키 정리
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtProvider.createAccessToken(
                String.valueOf(oAuth2User.getUserId()),
                oAuth2User.getAuthorities()
        );

        String refreshToken = jwtProvider.createRefreshToken(String.valueOf(oAuth2User.getUserId()));

        log.info("OAuth2 authentication success for user: {}", oAuth2User.getEmail());

        // 일회용 코드 생성 및 저장
        String code = UUID.randomUUID().toString();
        oAuth2CodePort.saveCode(code, accessToken, refreshToken);

        // 프론트엔드 리다이렉트 (일회용 코드만 전달)
        String targetUrl = frontendProperties.getOauth2RedirectUrl() + "?code=" + code;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
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
        var builder = org.springframework.http.ResponseCookie.from(name, value)
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
}
