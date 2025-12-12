package org.yyubin.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.yyubin.recommendation.config.RecommendationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 추천 결과 Redis 캐시 서비스
 * - Redis ZSET을 사용한 추천 결과 저장 및 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RecommendationProperties properties;

    private static final String RECOMMENDATION_KEY_PREFIX = "recommend:user:";
    private static final int MAX_CACHED_ITEMS = 100; // 최대 100개까지 캐시

    /**
     * 추천 결과를 Redis ZSET에 저장
     *
     * @param userId 사용자 ID
     * @param recommendations bookId -> score 맵
     */
    public void saveRecommendations(Long userId, java.util.Map<Long, Double> recommendations) {
        String key = getRecommendationKey(userId);

        try {
            // 기존 데이터 삭제
            redisTemplate.delete(key);

            // 새로운 데이터 저장
            for (var entry : recommendations.entrySet()) {
                String member = "book:" + entry.getKey();
                double score = entry.getValue();
                redisTemplate.opsForZSet().add(key, member, score);
            }

            // 상위 N개만 유지 (메모리 효율)
            long size = redisTemplate.opsForZSet().size(key);
            if (size > MAX_CACHED_ITEMS) {
                // 하위 항목 제거
                redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_CACHED_ITEMS - 1);
            }

            // TTL 설정
            int ttlHours = properties.getCache().getTtlHours();
            redisTemplate.expire(key, ttlHours, TimeUnit.HOURS);

            log.info("Saved {} recommendations for user {} (TTL: {}h)",
                    recommendations.size(), userId, ttlHours);

        } catch (Exception e) {
            log.error("Failed to save recommendations for user {}", userId, e);
        }
    }

    /**
     * 추천 결과 조회 (점수 높은 순)
     *
     * @param userId 사용자 ID
     * @param limit 조회할 개수
     * @return 추천 결과 리스트
     */
    public List<RecommendationResult> getRecommendations(Long userId, int limit) {
        String key = getRecommendationKey(userId);

        try {
            // 점수 높은 순으로 조회
            Set<ZSetOperations.TypedTuple<String>> results = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(key, 0, limit - 1);

            if (results == null || results.isEmpty()) {
                log.debug("No cached recommendations for user {}", userId);
                return List.of();
            }

            List<RecommendationResult> recommendations = new ArrayList<>();
            int rank = 1;

            for (ZSetOperations.TypedTuple<String> tuple : results) {
                String member = tuple.getValue();
                Double score = tuple.getScore();

                if (member != null && member.startsWith("book:")) {
                    Long bookId = Long.parseLong(member.substring(5));

                    recommendations.add(RecommendationResult.builder()
                            .bookId(bookId)
                            .score(score)
                            .rank(rank++)
                            .build());
                }
            }

            log.debug("Retrieved {} recommendations for user {}", recommendations.size(), userId);
            return recommendations;

        } catch (Exception e) {
            log.error("Failed to get recommendations for user {}", userId, e);
            return List.of();
        }
    }

    /**
     * 특정 도서의 추천 점수 조회
     *
     * @param userId 사용자 ID
     * @param bookId 도서 ID
     * @return 점수 (없으면 null)
     */
    public Double getBookScore(Long userId, Long bookId) {
        String key = getRecommendationKey(userId);
        String member = "book:" + bookId;

        try {
            return redisTemplate.opsForZSet().score(key, member);
        } catch (Exception e) {
            log.error("Failed to get book score for user {} book {}", userId, bookId, e);
            return null;
        }
    }

    /**
     * 추천 결과 삭제
     *
     * @param userId 사용자 ID
     */
    public void clearRecommendations(Long userId) {
        String key = getRecommendationKey(userId);
        redisTemplate.delete(key);
        log.info("Cleared recommendations for user {}", userId);
    }

    /**
     * 캐시 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @return 캐시 존재 여부
     */
    public boolean hasCachedRecommendations(Long userId) {
        String key = getRecommendationKey(userId);
        Long size = redisTemplate.opsForZSet().size(key);
        return size != null && size > 0;
    }

    /**
     * 캐시 통계
     */
    public CacheStats getCacheStats(Long userId) {
        String key = getRecommendationKey(userId);

        Long size = redisTemplate.opsForZSet().size(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        return CacheStats.builder()
                .userId(userId)
                .cachedItems(size != null ? size : 0)
                .ttlSeconds(ttl != null ? ttl : 0)
                .exists(size != null && size > 0)
                .build();
    }

    private String getRecommendationKey(Long userId) {
        return RECOMMENDATION_KEY_PREFIX + userId;
    }

    /**
     * 캐시 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private Long userId;
        private long cachedItems;
        private long ttlSeconds;
        private boolean exists;
    }
}
