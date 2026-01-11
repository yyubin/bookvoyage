package org.yyubin.infrastructure.recommendation.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.domain.recommendation.ReviewCircle;
import org.yyubin.domain.recommendation.ReviewCircleTopic;
import org.yyubin.domain.recommendation.SimilarUser;
import org.yyubin.domain.recommendation.UserTasteVector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 리뷰 서클 Redis 캐시 어댑터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCircleRedisCacheAdapter implements ReviewCircleCachePort {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TASTE_VECTOR_PREFIX = "review_circle:taste_vector:";
    private static final String SIMILAR_USERS_PREFIX = "review_circle:similar_users:";
    private static final String REVIEW_CIRCLE_PREFIX = "review_circle:topics:";

    private static final int TASTE_VECTOR_TTL_DAYS = 7;
    private static final int SIMILAR_USERS_TTL_DAYS = 1;
    private static final int REVIEW_CIRCLE_TTL_HOURS = 1;

    @Override
    public void saveTasteVector(UserTasteVector tasteVector) {
        String key = TASTE_VECTOR_PREFIX + tasteVector.userId();

        try {
            TasteVectorDto dto = TasteVectorDto.from(tasteVector);
            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.opsForValue().set(key, json, TASTE_VECTOR_TTL_DAYS, TimeUnit.DAYS);
            log.debug("Saved taste vector for user {} (TTL: {} days)", tasteVector.userId(), TASTE_VECTOR_TTL_DAYS);

        } catch (Exception e) {
            log.error("Failed to save taste vector for user {}", tasteVector.userId(), e);
        }
    }

    @Override
    public Optional<UserTasteVector> getTasteVector(Long userId) {
        String key = TASTE_VECTOR_PREFIX + userId;

        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }

            TasteVectorDto dto = objectMapper.readValue(json, TasteVectorDto.class);
            return Optional.of(dto.toDomain());

        } catch (Exception e) {
            log.error("Failed to get taste vector for user {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public void saveSimilarUsers(Long userId, List<SimilarUser> similarUsers) {
        String key = SIMILAR_USERS_PREFIX + userId;

        try {
            // ZSET 사용 (유사도 점수로 정렬)
            redisTemplate.delete(key);

            for (SimilarUser similarUser : similarUsers) {
                String member = String.valueOf(similarUser.userId());
                double score = similarUser.similarityScore();
                redisTemplate.opsForZSet().add(key, member, score);
            }

            redisTemplate.expire(key, SIMILAR_USERS_TTL_DAYS, TimeUnit.DAYS);
            log.debug("Saved {} similar users for user {} (TTL: {} days)",
                similarUsers.size(), userId, SIMILAR_USERS_TTL_DAYS);

        } catch (Exception e) {
            log.error("Failed to save similar users for user {}", userId, e);
        }
    }

    @Override
    public List<SimilarUser> getSimilarUsers(Long userId) {
        String key = SIMILAR_USERS_PREFIX + userId;

        try {
            // 점수 높은 순으로 조회
            var results = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);

            if (results == null || results.isEmpty()) {
                return List.of();
            }

            return results.stream()
                .filter(tuple -> tuple.getValue() != null && tuple.getScore() != null)
                .map(tuple -> SimilarUser.of(
                    Long.parseLong(tuple.getValue()),
                    tuple.getScore()
                ))
                .toList();

        } catch (Exception e) {
            log.error("Failed to get similar users for user {}", userId, e);
            return List.of();
        }
    }

    @Override
    public void saveReviewCircle(ReviewCircle reviewCircle) {
        String key = buildReviewCircleKey(reviewCircle.userId(), reviewCircle.window());

        try {
            ReviewCircleDto dto = ReviewCircleDto.from(reviewCircle);
            String json = objectMapper.writeValueAsString(dto);

            redisTemplate.opsForValue().set(key, json, REVIEW_CIRCLE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("Saved review circle for user {} window {} (TTL: {} hours)",
                reviewCircle.userId(), reviewCircle.window(), REVIEW_CIRCLE_TTL_HOURS);

        } catch (Exception e) {
            log.error("Failed to save review circle for user {} window {}",
                reviewCircle.userId(), reviewCircle.window(), e);
        }
    }

    @Override
    public Optional<ReviewCircle> getReviewCircle(Long userId, String window) {
        String key = buildReviewCircleKey(userId, window);

        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }

            ReviewCircleDto dto = objectMapper.readValue(json, ReviewCircleDto.class);
            return Optional.of(dto.toDomain());

        } catch (Exception e) {
            log.error("Failed to get review circle for user {} window {}", userId, window, e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteTasteVector(Long userId) {
        String key = TASTE_VECTOR_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Deleted taste vector for user {}", userId);
    }

    @Override
    public void deleteSimilarUsers(Long userId) {
        String key = SIMILAR_USERS_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Deleted similar users for user {}", userId);
    }

    @Override
    public void deleteReviewCircle(Long userId, String window) {
        String key = buildReviewCircleKey(userId, window);
        redisTemplate.delete(key);
        log.debug("Deleted review circle for user {} window {}", userId, window);
    }

    private String buildReviewCircleKey(Long userId, String window) {
        return REVIEW_CIRCLE_PREFIX + userId + ":" + window;
    }

    // DTOs for JSON serialization
    private record TasteVectorDto(
        Long userId,
        Map<String, Double> vector,
        String calculatedAt
    ) {
        static TasteVectorDto from(UserTasteVector domain) {
            return new TasteVectorDto(
                domain.userId(),
                domain.vector(),
                domain.calculatedAt().toString()
            );
        }

        UserTasteVector toDomain() {
            return new UserTasteVector(
                userId,
                vector,
                LocalDateTime.parse(calculatedAt)
            );
        }
    }

    private record ReviewCircleDto(
        Long userId,
        String window,
        List<ReviewCircleTopicDto> topics,
        int similarUserCount,
        String calculatedAt
    ) {
        static ReviewCircleDto from(ReviewCircle domain) {
            return new ReviewCircleDto(
                domain.userId(),
                domain.window(),
                domain.topics().stream().map(ReviewCircleTopicDto::from).toList(),
                domain.similarUserCount(),
                domain.calculatedAt().toString()
            );
        }

        ReviewCircle toDomain() {
            return new ReviewCircle(
                userId,
                window,
                topics.stream().map(ReviewCircleTopicDto::toDomain).toList(),
                similarUserCount,
                LocalDateTime.parse(calculatedAt)
            );
        }
    }

    private record ReviewCircleTopicDto(
        String keyword,
        int reviewCount,
        double score,
        String lastActivityAt
    ) {
        static ReviewCircleTopicDto from(ReviewCircleTopic domain) {
            return new ReviewCircleTopicDto(
                domain.keyword(),
                domain.reviewCount(),
                domain.score(),
                domain.lastActivityAt().toString()
            );
        }

        ReviewCircleTopic toDomain() {
            return ReviewCircleTopic.of(
                keyword,
                reviewCount,
                score,
                LocalDateTime.parse(lastActivityAt)
            );
        }
    }
}
