package org.yyubin.application.auth.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yyubin.application.auth.RefreshTokenUseCase;
import org.yyubin.application.auth.port.TokenBlacklistPort;
import org.yyubin.application.dto.AuthResult;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;
import org.yyubin.support.jwt.JwtProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final JwtProvider jwtProvider;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final LoadUserPort loadUserPort;

    @Override
    public AuthResult execute(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is required");
        }

        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        if (tokenBlacklistPort.isBlacklisted(refreshToken)) {
            throw new IllegalArgumentException("Refresh token is blacklisted");
        }

        String userId = jwtProvider.getUserIdFromToken(refreshToken);
        if (tokenBlacklistPort.isUserTokensInvalidated(userId)) {
            throw new IllegalArgumentException("User tokens invalidated");
        }

        User user = loadUserPort.loadById(new UserId(Long.parseLong(userId)));
        String roleName = user.role() != null ? user.role().name() : "USER";
        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));

        String newAccessToken = jwtProvider.createAccessToken(userId, authorities);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        try {
            tokenBlacklistPort.addToBlacklist(refreshToken);
        } catch (Exception e) {
            log.warn("Failed to blacklist refresh token during rotation", e);
        }

        return new AuthResult(
                newAccessToken,
                newRefreshToken,
                user.id().value(),
                user.email(),
                user.username()
        );
    }
}
