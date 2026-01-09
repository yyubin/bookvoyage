package org.yyubin.infrastructure.book.trending;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.dto.ShelfAdditionTrendResult;
import org.yyubin.application.book.port.ShelfAdditionCachePort;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShelfAdditionTrendCacheAdapter implements ShelfAdditionCachePort {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${book.trending.shelf-additions.cache-ttl-seconds:1800}")
    private long ttlSeconds;

    @Override
    public Optional<ShelfAdditionTrendResult> get(LocalDate date, ZoneId timezone, int limit) {
        String payload = stringRedisTemplate.opsForValue().get(cacheKey(date, timezone, limit));
        if (payload == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(payload, ShelfAdditionTrendResult.class));
        } catch (Exception e) {
            log.warn("Failed to parse shelf addition trend cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void put(LocalDate date, ZoneId timezone, int limit, ShelfAdditionTrendResult result) {
        try {
            String payload = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(
                    cacheKey(date, timezone, limit),
                    payload,
                    Duration.ofSeconds(ttlSeconds)
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize shelf addition trend cache", e);
        }
    }

    private String cacheKey(LocalDate date, ZoneId timezone, int limit) {
        return String.format("book:trending:shelf-additions:%s:%s:%d", date, timezone.getId(), limit);
    }
}
