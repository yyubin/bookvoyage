package org.yyubin.infrastructure.stream.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 트래킹 이벤트를 간단히 Redis에 누적
 */
@Component
@RequiredArgsConstructor
public class ReviewTrackingCounterAdapter {

    private final RedisTemplate<String, String> redisTemplate;

    public void incrementImpression(Long reviewId) {
        incr("metric:review:impression:" + reviewId);
    }

    public void incrementReach(Long reviewId) {
        incr("metric:review:reach:" + reviewId);
    }

    public void incrementClick(Long reviewId) {
        incr("metric:review:click:" + reviewId);
    }

    public void addDwell(Long reviewId, long dwellMs) {
        incrBy("metric:review:dwell:sum:" + reviewId, dwellMs);
        incr("metric:review:dwell:count:" + reviewId);
    }

    private void incr(String key) {
        if (key == null) return;
        redisTemplate.opsForValue().increment(key);
    }

    private void incrBy(String key, long value) {
        if (key == null) return;
        redisTemplate.opsForValue().increment(key, value);
    }
}
