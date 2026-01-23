package org.yyubin.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;
import org.yyubin.support.jwt.JwtProperties;
import org.yyubin.support.jwt.JwtProvider;
import org.yyubin.support.web.CookieProperties;
import org.yyubin.support.web.FrontendProperties;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;
    private final CookieProperties cookieProperties;
    private final FrontendProperties frontendProperties;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

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

        // 토큰을 쿠키에 설정
        setTokenCookies(response, accessToken, refreshToken);

        log.info("OAuth2 authentication success for user: {}", oAuth2User.getEmail());

        // 프론트엔드 리다이렉트 URL (토큰은 쿠키로 전달됨, 환경변수로 설정)
        String targetUrl = frontendProperties.getOauth2RedirectUrl();
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
