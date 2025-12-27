package org.yyubin.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.yyubin.application.auth.port.TokenBlacklistPort;
import org.yyubin.support.jwt.JwtProvider;

import java.util.concurrent.TimeUnit;

/**
 * JWT 토큰 블랙리스트 관리 서비스
 * Redis를 사용하여 로그아웃된 토큰을 추적하고 무효화합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService implements TokenBlacklistPort {

    private static final String BLACKLIST_PREFIX = "blacklist:jwt:";
    private static final String USER_BLACKLIST_PREFIX = "blacklist:user:";

    private final RedissonClient redissonClient;
    private final JwtProvider jwtProvider;

    /**
     * 토큰을 블랙리스트에 추가
     *
     * @param token JWT 토큰
     */
    public void addToBlacklist(String token) {
        try {
            String jti = jwtProvider.getJtiFromToken(token);
            String userId = jwtProvider.getUserIdFromToken(token);
            long ttl = jwtProvider.getRemainingTimeInSeconds(token);

            if (ttl > 0) {
                String key = BLACKLIST_PREFIX + jti;
                RBucket<String> bucket = redissonClient.getBucket(key);
                bucket.set(userId, ttl, TimeUnit.SECONDS);

                log.info("Token added to blacklist - JTI: {}, UserId: {}, TTL: {}s", jti, userId, ttl);
            } else {
                log.debug("Token already expired, skipping blacklist - JTI: {}", jti);
            }
        } catch (Exception e) {
            log.error("Failed to add token to blacklist", e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     *
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String token) {
        try {
            String jti = jwtProvider.getJtiFromToken(token);
            String key = BLACKLIST_PREFIX + jti;
            RBucket<String> bucket = redissonClient.getBucket(key);

            boolean exists = bucket.isExists();
            if (exists) {
                log.warn("Blacklisted token detected - JTI: {}", jti);
            }
            return exists;
        } catch (Exception e) {
            log.error("Error checking blacklist for token", e);
            // 에러 발생 시 안전하게 차단 (Fail-safe)
            return true;
        }
    }

    /**
     * 특정 사용자의 모든 토큰 무효화
     * 패스워드 변경, 계정 정지 등의 상황에서 사용
     *
     * @param userId 사용자 ID
     * @param maxTokenLifetimeSeconds 최대 토큰 유효 시간 (초)
     */
    public void invalidateAllUserTokens(String userId, long maxTokenLifetimeSeconds) {
        try {
            String key = USER_BLACKLIST_PREFIX + userId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            bucket.set("all", maxTokenLifetimeSeconds, TimeUnit.SECONDS);

            log.warn("All tokens invalidated for user: {}, TTL: {}s", userId, maxTokenLifetimeSeconds);
        } catch (Exception e) {
            log.error("Failed to invalidate all user tokens for userId: {}", userId, e);
            throw new RuntimeException("Failed to invalidate user tokens", e);
        }
    }

    /**
     * 사용자의 모든 토큰이 무효화되었는지 확인
     *
     * @param userId 사용자 ID
     * @return 무효화되었으면 true, 아니면 false
     */
    public boolean isUserTokensInvalidated(String userId) {
        try {
            String key = USER_BLACKLIST_PREFIX + userId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            return bucket.isExists();
        } catch (Exception e) {
            log.error("Error checking user tokens invalidation for userId: {}", userId, e);
            // 에러 발생 시 안전하게 차단 (Fail-safe)
            return true;
        }
    }

    /**
     * 블랙리스트에서 특정 토큰 제거 (관리자 기능)
     *
     * @param token JWT 토큰
     */
    public void removeFromBlacklist(String token) {
        try {
            String jti = jwtProvider.getJtiFromToken(token);
            String key = BLACKLIST_PREFIX + jti;
            RBucket<String> bucket = redissonClient.getBucket(key);
            bucket.delete();

            log.info("Token removed from blacklist - JTI: {}", jti);
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist", e);
            throw new RuntimeException("Failed to remove token from blacklist", e);
        }
    }

    /**
     * 사용자 전체 토큰 무효화 해제 (관리자 기능)
     *
     * @param userId 사용자 ID
     */
    public void restoreUserTokens(String userId) {
        try {
            String key = USER_BLACKLIST_PREFIX + userId;
            RBucket<String> bucket = redissonClient.getBucket(key);
            bucket.delete();

            log.info("User tokens restored - UserId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to restore user tokens for userId: {}", userId, e);
            throw new RuntimeException("Failed to restore user tokens", e);
        }
    }
}
