package org.yyubin.infrastructure.stream.metric;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.ReviewViewMetricPort;
import org.yyubin.infrastructure.config.MetricProperties;

@Component
@RequiredArgsConstructor
public class ReviewViewMetricAdapter implements ReviewViewMetricPort {

    private final RedisTemplate<String, String> redisTemplate;
    private final MetricProperties metricProperties;

    @Override
    public long incrementAndGet(Long reviewId, Long userId) {
        if (reviewId == null) {
            return 0L;
        }

        // 사용자 중복 방지 (24h)
        if (userId != null) {
            String dedupKey = dedupKey(reviewId);
            Long added = redisTemplate.opsForSet().add(dedupKey, userId.toString());
            redisTemplate.expire(dedupKey, java.time.Duration.ofSeconds(metricProperties.getDedupTtlSeconds()));
            if (added != null && added == 0) {
                // 이미 본 사용자면 현재 카운터 반환
                return getCachedCount(reviewId).orElse(0L);
            }
        }

        Long value = redisTemplate.opsForValue().increment(counterKey(reviewId));
        redisTemplate.expire(counterKey(reviewId), java.time.Duration.ofSeconds(metricProperties.getCounterTtlSeconds()));
        return value != null ? value : 0L;
    }

    @Override
    public Optional<Long> getCachedCount(Long reviewId) {
        if (reviewId == null) {
            return Optional.empty();
        }
        Object value = redisTemplate.opsForValue().get(counterKey(reviewId));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(value.toString()));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private String counterKey(Long reviewId) {
        return "metric:review:view:" + reviewId;
    }

    private String dedupKey(Long reviewId) {
        return "metric:review:viewdedup:" + reviewId;
    }
}
