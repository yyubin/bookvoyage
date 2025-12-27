package org.yyubin.application.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.yyubin.application.auth.LogoutUseCase;
import org.yyubin.application.auth.port.TokenBlacklistPort;

/**
 * 로그아웃 서비스
 * JWT 토큰을 블랙리스트에 추가하여 무효화합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    public void execute(String accessToken, String refreshToken) {
        int successCount = 0;
        int failCount = 0;

        // 1. Access Token 블랙리스트 추가
        if (StringUtils.hasText(accessToken)) {
            try {
                tokenBlacklistPort.addToBlacklist(accessToken);
                successCount++;
                log.debug("Access token added to blacklist");
            } catch (Exception e) {
                failCount++;
                log.error("Failed to add access token to blacklist", e);
                // 블랙리스트 실패해도 계속 진행 (Refresh Token 처리)
            }
        }

        // 2. Refresh Token 블랙리스트 추가
        if (StringUtils.hasText(refreshToken)) {
            try {
                tokenBlacklistPort.addToBlacklist(refreshToken);
                successCount++;
                log.debug("Refresh token added to blacklist");
            } catch (Exception e) {
                failCount++;
                log.error("Failed to add refresh token to blacklist", e);
            }
        }

        if (successCount > 0) {
            log.info("Logout completed - blacklisted {} token(s)", successCount);
        }

        if (failCount > 0) {
            log.warn("Logout partially failed - {} token(s) failed to blacklist", failCount);
        }
    }
}
