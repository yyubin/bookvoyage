package org.yyubin.recommendation.service;

import java.util.ArrayList;
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
 * 리뷰 추천 결과 캐시
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewRecommendationCacheService {

    private static final String KEY_PREFIX = "recommend:review:user:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ReviewRecommendationProperties properties;

    public void save(Long userId, Long bookContextId, java.util.Map<Long, Double> scores) {
        String key = key(userId, bookContextId);
        try {
            redisTemplate.delete(key);

            scores.forEach((reviewId, score) -> {
                if (reviewId != null) {
                    redisTemplate.opsForZSet().add(key, "review:" + reviewId, score);
                }
            });

            long size = redisTemplate.opsForZSet().size(key);
            int maxItems = properties.getCache().getMaxItems();
            if (size > maxItems) {
                redisTemplate.opsForZSet().removeRange(key, 0, size - maxItems - 1);
            }

            redisTemplate.expire(key, properties.getCache().getTtlHours(), TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Failed to cache review recommendations for user {}", userId, e);
        }
    }

    public List<ReviewRecommendationResult> get(Long userId, Long bookContextId, int limit) {
        return get(userId, bookContextId, null, limit);
    }

    public List<ReviewRecommendationResult> get(Long userId, Long bookContextId, Long cursor, int limit) {
        String key = key(userId, bookContextId);
        try {
            // 전체 결과를 가져옴
            Set<ZSetOperations.TypedTuple<String>> allTuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
            if (allTuples == null || allTuples.isEmpty()) {
                return List.of();
            }

            List<ReviewRecommendationResult> results = new ArrayList<>();
            boolean foundCursor = cursor == null;
            int rank = 1;

            for (ZSetOperations.TypedTuple<String> tuple : allTuples) {
                if (tuple.getValue() != null && tuple.getValue().startsWith("review:")) {
                    Long reviewId = Long.parseLong(tuple.getValue().substring("review:".length()));

                    if (foundCursor) {
                        results.add(ReviewRecommendationResult.builder()
                                .reviewId(reviewId)
                                .score(tuple.getScore())
                                .rank(rank++)
                                .build());

                        if (results.size() >= limit) {
                            break;
                        }
                    } else if (reviewId.equals(cursor)) {
                        foundCursor = true;
                    }
                }
            }
            return results;
        } catch (Exception e) {
            log.warn("Failed to read review recommendations for user {}", userId, e);
            return List.of();
        }
    }

    public boolean exists(Long userId, Long bookContextId) {
        String key = key(userId, bookContextId);
        Long size = redisTemplate.opsForZSet().size(key);
        return size != null && size > 0;
    }

    public void clear(Long userId, Long bookContextId) {
        redisTemplate.delete(key(userId, bookContextId));
    }

    private String key(Long userId, Long bookContextId) {
        String contextPart = bookContextId != null ? (":book:" + bookContextId) : ":feed";
        return KEY_PREFIX + userId + contextPart;
    }
}
