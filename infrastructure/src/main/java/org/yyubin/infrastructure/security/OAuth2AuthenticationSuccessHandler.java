package org.yyubin.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect.");
            return;
        }

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String accessToken = jwtProvider.createAccessToken(
                String.valueOf(oAuth2User.getUserId()),
                oAuth2User.getAuthorities()
        );

        String refreshToken = jwtProvider.createRefreshToken(String.valueOf(oAuth2User.getUserId()));

        // 토큰을 쿠키에 설정
        setTokenCookies(response, accessToken, refreshToken);

        log.info("OAuth2 authentication success for user: {}", oAuth2User.getEmail());

        // 프론트엔드 리다이렉트 URL (토큰은 쿠키로 전달됨)
        String targetUrl = "/oauth2/redirect";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
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
        return cookie;
    }
}
