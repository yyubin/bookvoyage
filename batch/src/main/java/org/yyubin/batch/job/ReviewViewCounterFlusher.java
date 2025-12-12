package org.yyubin.batch.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.recommendation.port.ReviewViewFlushPort;

/**
 * Redis 조회수 카운터를 DB/ES에 플러시
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewViewCounterFlusher {

    private final RedisTemplate<String, String> redisTemplate;
    private final ReviewViewFlushPort reviewViewFlushPort;

    private static final String COUNTER_PREFIX = "metric:review:view:";

    @Transactional
    public void flush() {
        List<CounterUpdate> updates = readCounters();
        if (updates.isEmpty()) {
            return;
        }

        // DB 업데이트
        updates.forEach(update -> {
            long base = reviewViewFlushPort.findCurrentViewCount(update.reviewId()).orElse(0L);
            long newCount = base + update.delta();
            reviewViewFlushPort.updateViewCount(update.reviewId(), newCount);
        });

        // ES 업데이트 (partial)
        Map<Long, Long> esUpdates = new HashMap<>();
        updates.forEach(u -> esUpdates.merge(u.reviewId(), u.delta(), Long::sum));
        reviewViewFlushPort.updateSearchIndexViewCount(esUpdates);

        log.info("Flushed {} review view counters to DB/ES", updates.size());
    }

    private List<CounterUpdate> readCounters() {
        List<CounterUpdate> updates = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions().match(COUNTER_PREFIX + "*").count(1000).build();
        try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(options)) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                Object raw = redisTemplate.opsForValue().get(key);
                Long value = parseLong(raw);
                if (value == null || value <= 0) {
                    continue;
                }
                redisTemplate.delete(key);
                Long reviewId = parseReviewId(key);
                if (reviewId != null) {
                    updates.add(new CounterUpdate(reviewId, value));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to scan review view counters", e);
        }
        return updates;
    }

    private Long parseReviewId(String key) {
        try {
            return Long.parseLong(key.substring(COUNTER_PREFIX.length()));
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(Object raw) {
        if (raw == null) {
            return null;
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record CounterUpdate(Long reviewId, long delta) { }
}
