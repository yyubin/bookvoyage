package org.yyubin.recommendation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRecommendationCacheService 테스트")
class ReviewRecommendationCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private ReviewRecommendationProperties properties;

    @Mock
    private ReviewRecommendationProperties.Cache cacheConfig;

    @InjectMocks
    private ReviewRecommendationCacheService cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Nested
    @DisplayName("save 테스트")
    class SaveTest {

        @Test
        @DisplayName("피드 컨텍스트의 추천 결과를 저장한다")
        void save_FeedContext() {
            // Given
            Long userId = 1L;
            Long bookContextId = null; // feed context
            Map<Long, Double> scores = Map.of(100L, 0.95, 101L, 0.85);

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getMaxItems()).thenReturn(100);
            when(cacheConfig.getTtlHours()).thenReturn(2);
            when(zSetOperations.size(anyString())).thenReturn(2L);

            // When
            cacheService.save(userId, bookContextId, scores);

            // Then
            verify(redisTemplate).delete("recommend:review:user:1:feed");
            verify(zSetOperations, times(2)).add(eq("recommend:review:user:1:feed"), anyString(), anyDouble());
            verify(redisTemplate).expire(eq("recommend:review:user:1:feed"), eq(2L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("도서 컨텍스트의 추천 결과를 저장한다")
        void save_BookContext() {
            // Given
            Long userId = 1L;
            Long bookContextId = 500L;
            Map<Long, Double> scores = Map.of(100L, 0.95);

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getMaxItems()).thenReturn(100);
            when(cacheConfig.getTtlHours()).thenReturn(2);
            when(zSetOperations.size(anyString())).thenReturn(1L);

            // When
            cacheService.save(userId, bookContextId, scores);

            // Then
            verify(redisTemplate).delete("recommend:review:user:1:book:500");
            verify(zSetOperations).add("recommend:review:user:1:book:500", "review:100", 0.95);
        }

        @Test
        @DisplayName("null reviewId는 저장하지 않는다")
        void save_SkipsNullReviewId() {
            // Given
            Long userId = 1L;
            Map<Long, Double> scores = new HashMap<>();
            scores.put(null, 0.95);
            scores.put(100L, 0.85);

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getMaxItems()).thenReturn(100);
            when(cacheConfig.getTtlHours()).thenReturn(2);
            when(zSetOperations.size(anyString())).thenReturn(1L);

            // When
            cacheService.save(userId, null, scores);

            // Then
            verify(zSetOperations, times(1)).add(anyString(), eq("review:100"), eq(0.85));
        }

        @Test
        @DisplayName("maxItems 초과시 하위 항목을 제거한다")
        void save_TrimsExcess() {
            // Given
            Long userId = 1L;
            Map<Long, Double> scores = new HashMap<>();
            for (int i = 0; i < 150; i++) {
                scores.put((long) i, 0.5);
            }

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getMaxItems()).thenReturn(100);
            when(cacheConfig.getTtlHours()).thenReturn(2);
            when(zSetOperations.size(anyString())).thenReturn(150L);

            // When
            cacheService.save(userId, null, scores);

            // Then
            verify(zSetOperations).removeRange(anyString(), eq(0L), eq(49L));
        }

        @Test
        @DisplayName("예외 발생시 로그만 남기고 실패하지 않는다")
        void save_HandlesException() {
            // Given
            Long userId = 1L;
            Map<Long, Double> scores = Map.of(100L, 0.95);

            when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis error"));

            // When & Then (no exception thrown)
            cacheService.save(userId, null, scores);
        }
    }

    @Nested
    @DisplayName("get 테스트")
    class GetTest {

        @Test
        @DisplayName("캐시된 리뷰 추천 결과를 조회한다")
        void get_Success() {
            // Given
            Long userId = 1L;
            Long bookContextId = null;
            int limit = 3;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("review:100", 0.95));
            tuples.add(createTuple("review:101", 0.85));
            tuples.add(createTuple("review:102", 0.75));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            List<ReviewRecommendationResult> results = cacheService.get(userId, bookContextId, limit);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).getReviewId()).isEqualTo(100L);
            assertThat(results.get(0).getScore()).isEqualTo(0.95);
            assertThat(results.get(0).getRank()).isEqualTo(1);
        }

        @Test
        @DisplayName("cursor 기반 페이징이 동작한다")
        void get_WithCursor() {
            // Given
            Long userId = 1L;
            Long cursor = 100L;
            int limit = 2;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("review:100", 0.95));
            tuples.add(createTuple("review:101", 0.85));
            tuples.add(createTuple("review:102", 0.75));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            List<ReviewRecommendationResult> results = cacheService.get(userId, null, cursor, limit);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getReviewId()).isEqualTo(101L);
            assertThat(results.get(1).getReviewId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("빈 캐시는 빈 리스트를 반환한다")
        void get_EmptyCache() {
            // Given
            Long userId = 1L;
            when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .thenReturn(null);

            // When
            List<ReviewRecommendationResult> results = cacheService.get(userId, null, 10);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("빈 Set은 빈 리스트를 반환한다")
        void get_EmptySet() {
            // Given
            Long userId = 1L;
            when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .thenReturn(Collections.emptySet());

            // When
            List<ReviewRecommendationResult> results = cacheService.get(userId, null, 10);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("잘못된 member 형식은 무시한다")
        void get_IgnoresInvalidMember() {
            // Given
            Long userId = 1L;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("review:100", 0.95));
            tuples.add(createTuple("invalid:200", 0.85));
            tuples.add(createTuple(null, 0.75));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            List<ReviewRecommendationResult> results = cacheService.get(userId, null, 10);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getReviewId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("예외 발생시 빈 리스트를 반환한다")
        void get_ReturnsEmptyOnException() {
            // Given
            Long userId = 1L;
            when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Redis error"));

            // When
            List<ReviewRecommendationResult> results = cacheService.get(userId, null, 10);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("도서 컨텍스트 키를 사용한다")
        void get_BookContextKey() {
            // Given
            Long userId = 1L;
            Long bookContextId = 500L;

            when(zSetOperations.reverseRangeWithScores(eq("recommend:review:user:1:book:500"), anyLong(), anyLong()))
                    .thenReturn(Collections.emptySet());

            // When
            cacheService.get(userId, bookContextId, 10);

            // Then
            verify(zSetOperations).reverseRangeWithScores("recommend:review:user:1:book:500", 0L, -1L);
        }
    }

    @Nested
    @DisplayName("exists 테스트")
    class ExistsTest {

        @Test
        @DisplayName("캐시가 존재하면 true를 반환한다")
        void exists_True() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:review:user:1:feed")).thenReturn(10L);

            // When
            boolean result = cacheService.exists(userId, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("캐시가 비어있으면 false를 반환한다")
        void exists_False_Empty() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:review:user:1:feed")).thenReturn(0L);

            // When
            boolean result = cacheService.exists(userId, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("캐시가 null이면 false를 반환한다")
        void exists_False_Null() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:review:user:1:feed")).thenReturn(null);

            // When
            boolean result = cacheService.exists(userId, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("도서 컨텍스트 키로 확인한다")
        void exists_BookContext() {
            // Given
            Long userId = 1L;
            Long bookContextId = 500L;
            when(zSetOperations.size("recommend:review:user:1:book:500")).thenReturn(5L);

            // When
            boolean result = cacheService.exists(userId, bookContextId);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("clear 테스트")
    class ClearTest {

        @Test
        @DisplayName("피드 컨텍스트 캐시를 삭제한다")
        void clear_FeedContext() {
            // Given
            Long userId = 1L;

            // When
            cacheService.clear(userId, null);

            // Then
            verify(redisTemplate).delete("recommend:review:user:1:feed");
        }

        @Test
        @DisplayName("도서 컨텍스트 캐시를 삭제한다")
        void clear_BookContext() {
            // Given
            Long userId = 1L;
            Long bookContextId = 500L;

            // When
            cacheService.clear(userId, bookContextId);

            // Then
            verify(redisTemplate).delete("recommend:review:user:1:book:500");
        }
    }

    private ZSetOperations.TypedTuple<String> createTuple(String value, Double score) {
        return new ZSetOperations.TypedTuple<>() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public Double getScore() {
                return score;
            }

            @Override
            public int compareTo(ZSetOperations.TypedTuple<String> o) {
                return Double.compare(o.getScore(), score);
            }
        };
    }
}
