package org.yyubin.infrastructure.search.trending;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.yyubin.application.search.port.SearchKeywordTrendingPort;
import org.yyubin.domain.search.TrendingKeyword;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordTrendingAdapter implements SearchKeywordTrendingPort {

    private static final String KEY_PREFIX = "search:trending";
    private static final DateTimeFormatter HOURLY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHH");
    private static final DateTimeFormatter DAILY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter WEEKLY_FORMAT = DateTimeFormatter.ofPattern("yyyyww");

    private final StringRedisTemplate redisTemplate;

    @Override
    public void incrementKeywordScore(String normalizedKeyword, double score, Duration ttl) {
        LocalDateTime now = LocalDateTime.now();

        // Increment in hourly window
        String hourlyKey = buildKey(TimeWindow.HOURLY, now);
        redisTemplate.opsForZSet().incrementScore(hourlyKey, normalizedKeyword, score);
        redisTemplate.expire(hourlyKey, Duration.ofHours(2));

        // Increment in daily window
        String dailyKey = buildKey(TimeWindow.DAILY, now);
        redisTemplate.opsForZSet().incrementScore(dailyKey, normalizedKeyword, score);
        redisTemplate.expire(dailyKey, Duration.ofDays(2));

        // Increment in weekly window
        String weeklyKey = buildKey(TimeWindow.WEEKLY, now);
        redisTemplate.opsForZSet().incrementScore(weeklyKey, normalizedKeyword, score);
        redisTemplate.expire(weeklyKey, Duration.ofDays(8));
    }

    @Override
    public List<TrendingKeyword> getTopKeywords(int limit) {
        return getTopKeywordsByWindow(TimeWindow.DAILY, limit);
    }

    @Override
    public List<TrendingKeyword> getTopKeywordsByWindow(TimeWindow window, int limit) {
        String key = buildKey(window, LocalDateTime.now());
        Set<ZSetOperations.TypedTuple<String>> results =
            redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);

        if (results == null || results.isEmpty()) {
            return List.of();
        }

        List<TrendingKeyword> keywords = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> result : results) {
            if (result.getValue() != null && result.getScore() != null) {
                keywords.add(TrendingKeyword.of(
                    result.getValue(),
                    result.getScore().longValue(),
                    rank++
                ));
            }
        }

        return keywords;
    }

    @Override
    public void clear(TimeWindow window) {
        String key = buildKey(window, LocalDateTime.now());
        redisTemplate.delete(key);
        log.info("Cleared trending keywords for window: {}", window);
    }

    private String buildKey(TimeWindow window, LocalDateTime time) {
        return switch (window) {
            case HOURLY -> String.format("%s:hourly:%s", KEY_PREFIX, time.format(HOURLY_FORMAT));
            case DAILY -> String.format("%s:daily:%s", KEY_PREFIX, time.format(DAILY_FORMAT));
            case WEEKLY -> String.format("%s:weekly:%s", KEY_PREFIX, time.format(WEEKLY_FORMAT));
        };
    }
}
