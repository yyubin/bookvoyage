package org.yyubin.batch.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 기반 리뷰 참여도 집계 (CTR/도달/평균 dwell)
 * 간단히 metric 키에서 가져와 배치 색인에 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEngagementStatsProvider {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String KEY_IMPRESSION = "metric:review:impression:";
    private static final String KEY_REACH = "metric:review:reach:";
    private static final String KEY_CLICK = "metric:review:click:";
    private static final String KEY_DWELL_SUM = "metric:review:dwell:sum:";
    private static final String KEY_DWELL_COUNT = "metric:review:dwell:count:";

    public ReviewEngagementStats getStats(Long reviewId) {
        if (reviewId == null) {
            return new ReviewEngagementStats(0, 0, 0, 0, 0);
        }
        long impressions = getLong(KEY_IMPRESSION + reviewId);
        long reaches = getLong(KEY_REACH + reviewId);
        long clicks = getLong(KEY_CLICK + reviewId);
        long dwellSum = getLong(KEY_DWELL_SUM + reviewId);
        long dwellCount = getLong(KEY_DWELL_COUNT + reviewId);
        return new ReviewEngagementStats(impressions, reaches, clicks, dwellSum, dwellCount);
    }

    private long getLong(String key) {
        Object raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            return 0L;
        }
        try {
            return Long.parseLong(raw.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
