package org.yyubin.infrastructure.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.auth.port.OAuth2CodePort;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2CodeAdapter implements OAuth2CodePort {

    private static final String OAUTH2_CODE_PREFIX = "oauth2:code:";
    private static final Duration CODE_TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveCode(String code, String accessToken, String refreshToken) {
        String key = OAUTH2_CODE_PREFIX + code;
        String tokenData = accessToken + ":" + refreshToken;
        log.info("Saving OAuth2 code to Redis, key: {}", key);
        redisTemplate.opsForValue().set(key, tokenData, CODE_TTL);
        log.info("OAuth2 code saved successfully");
    }

    @Override
    public Optional<String> getAndDeleteCode(String code) {
        String key = OAUTH2_CODE_PREFIX + code;
        log.info("Getting OAuth2 code from Redis, key: {}", key);

        String tokenData = redisTemplate.opsForValue().get(key);
        log.info("Redis returned: {}", tokenData != null ? "data present (length: " + tokenData.length() + ")" : "null");

        if (tokenData != null) {
            // 일회용 코드 삭제
            redisTemplate.delete(key);
            log.info("OAuth2 code deleted from Redis");
            return Optional.of(tokenData);
        }

        log.warn("OAuth2 code not found in Redis");
        return Optional.empty();
    }
}
