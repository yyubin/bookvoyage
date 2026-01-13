package org.yyubin.recommendation.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;

/**
 * 최근 노출된 리뷰 기록/조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRecommendationExposureService {

    private static final String KEY_PREFIX = "recommend:review:exposed:user:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ReviewRecommendationProperties properties;

    public Set<Long> loadRecentReviewIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        int limit = properties.getSearch().getExposureFilterLimit();
        String key = key(userId);

        try {
            Set<String> raw = redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
            if (raw == null || raw.isEmpty()) {
                return Set.of();
            }
            Set<Long> result = new LinkedHashSet<>();
            for (String value : raw) {
                Long reviewId = parseLong(value);
                if (reviewId != null) {
                    result.add(reviewId);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("Failed to load recent review exposures for user {}", userId, e);
            return Set.of();
        }
    }

    public void recordExposure(Long userId, List<Long> reviewIds) {
        if (userId == null || reviewIds == null || reviewIds.isEmpty()) {
            return;
        }
        String key = key(userId);
        long now = System.currentTimeMillis();

        try {
            for (Long reviewId : reviewIds) {
                if (reviewId != null) {
                    redisTemplate.opsForZSet().add(key, String.valueOf(reviewId), now);
                }
            }

            Long size = redisTemplate.opsForZSet().size(key);
            int maxItems = properties.getSearch().getExposureMaxItems();
            if (size != null && size > maxItems) {
                redisTemplate.opsForZSet().removeRange(key, 0, size - maxItems - 1);
            }

            redisTemplate.expire(key, properties.getSearch().getExposureTtlHours(), TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Failed to record review exposures for user {}", userId, e);
        }
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
