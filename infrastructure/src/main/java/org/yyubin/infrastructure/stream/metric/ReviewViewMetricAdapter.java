package org.yyubin.infrastructure.stream.metric;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.ReviewViewFlushPort;
import org.yyubin.application.review.port.ReviewViewMetricPort;
import org.yyubin.infrastructure.config.MetricProperties;

@Component
@RequiredArgsConstructor
public class ReviewViewMetricAdapter implements ReviewViewMetricPort {

    private final RedisTemplate<String, String> redisTemplate;
    private final MetricProperties metricProperties;
    private final ReviewViewFlushPort reviewViewFlushPort;

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
                return getCountWithFallback(reviewId);
            }
        }

        ensureCounterInitialized(reviewId);
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

    @Override
    public Map<Long, Long> getBatchCachedCounts(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>();

        // Fetch values one by one or use pipeline
        for (Long reviewId : reviewIds) {
            Object value = redisTemplate.opsForValue().get(counterKey(reviewId));
            if (value != null) {
                try {
                    result.put(reviewId, Long.parseLong(value.toString()));
                } catch (NumberFormatException ignored) {
                    // Skip invalid values
                }
            }
        }

        return result;
    }

    @Override
    public long getCountWithFallback(Long reviewId) {
        return getCachedCount(reviewId)
            .orElseGet(() -> reviewViewFlushPort.findCurrentViewCount(reviewId).orElse(0L));
    }

    @Override
    public Map<Long, Long> getBatchCountsWithFallback(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> result = new HashMap<>(getBatchCachedCounts(reviewIds));
        Set<Long> cachedIds = result.keySet();

        for (Long reviewId : reviewIds) {
            if (cachedIds.contains(reviewId)) {
                continue;
            }
            reviewViewFlushPort.findCurrentViewCount(reviewId)
                .ifPresent(count -> result.put(reviewId, count));
        }

        return result;
    }

    private String counterKey(Long reviewId) {
        return "metric:review:view:" + reviewId;
    }

    private String dedupKey(Long reviewId) {
        return "metric:review:viewdedup:" + reviewId;
    }

    private void ensureCounterInitialized(Long reviewId) {
        if (getCachedCount(reviewId).isPresent()) {
            return;
        }
        long base = reviewViewFlushPort.findCurrentViewCount(reviewId).orElse(0L);
        redisTemplate.opsForValue().setIfAbsent(
            counterKey(reviewId),
            Long.toString(base),
            java.time.Duration.ofSeconds(metricProperties.getCounterTtlSeconds())
        );
    }
}
