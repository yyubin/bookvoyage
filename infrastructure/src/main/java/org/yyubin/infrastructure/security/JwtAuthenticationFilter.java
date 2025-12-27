package org.yyubin.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.yyubin.application.auth.port.TokenBlacklistPort;
import org.yyubin.support.jwt.JwtProvider;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (StringUtils.hasText(token)) {
            // 1. 서명 및 만료 검증
            if (!jwtProvider.validateToken(token)) {
                log.debug("Invalid JWT token, uri: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 블랙리스트 확인
            if (tokenBlacklistPort.isBlacklisted(token)) {
                log.warn("Blacklisted token attempted, uri: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 사용자 전체 토큰 무효화 확인
            String userId = jwtProvider.getUserIdFromToken(token);
            if (tokenBlacklistPort.isUserTokensInvalidated(userId)) {
                log.warn("User tokens invalidated, userId: {}, uri: {}", userId, request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            // 4. 인증 설정
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set Authentication to security context for '{}', uri: {}",
                    authentication.getName(), request.getRequestURI());
        } else {
            log.debug("No JWT token found, uri: {}", request.getRequestURI());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더에서 토큰 추출
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        // 2. 쿠키에서 accessToken 추출
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "accessToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
