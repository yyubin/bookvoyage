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
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.sampling.WindowSampler;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationCacheService 테스트")
class RecommendationCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @Mock
    private RecommendationProperties properties;

    @Mock
    private RecommendationProperties.CacheConfig cacheConfig;

    @Mock
    private WindowSampler windowSampler;

    @InjectMocks
    private RecommendationCacheService cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Nested
    @DisplayName("saveRecommendations 테스트")
    class SaveRecommendationsTest {

        @Test
        @DisplayName("추천 결과를 Redis ZSET에 저장한다")
        void save_Success() {
            // Given
            Long userId = 1L;
            Map<Long, Double> recommendations = Map.of(
                    100L, 0.95,
                    101L, 0.85,
                    102L, 0.75
            );

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getTtlHours()).thenReturn(3);
            when(zSetOperations.size(anyString())).thenReturn(3L);

            // When
            cacheService.saveRecommendations(userId, recommendations);

            // Then
            verify(redisTemplate).delete("recommend:user:1");
            verify(zSetOperations, times(3)).add(eq("recommend:user:1"), anyString(), anyDouble());
            verify(redisTemplate).expire(eq("recommend:user:1"), eq(3L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("100개 초과시 하위 항목을 제거한다")
        void save_TrimsExcess() {
            // Given
            Long userId = 1L;
            Map<Long, Double> recommendations = new HashMap<>();
            for (int i = 0; i < 150; i++) {
                recommendations.put((long) i, 0.5);
            }

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getTtlHours()).thenReturn(3);
            when(zSetOperations.size(anyString())).thenReturn(150L);

            // When
            cacheService.saveRecommendations(userId, recommendations);

            // Then
            verify(zSetOperations).removeRange(eq("recommend:user:1"), eq(0L), eq(49L));
        }

        @Test
        @DisplayName("예외 발생시 로그만 남기고 실패하지 않는다")
        void save_HandlesException() {
            // Given
            Long userId = 1L;
            Map<Long, Double> recommendations = Map.of(100L, 0.95);

            when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis error"));

            // When & Then (no exception thrown)
            cacheService.saveRecommendations(userId, recommendations);
        }
    }

    @Nested
    @DisplayName("getRecommendations 테스트")
    class GetRecommendationsTest {

        @Test
        @DisplayName("캐시된 추천 결과를 조회한다")
        void get_Success() {
            // Given
            Long userId = 1L;
            int limit = 3;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("book:100", 0.95));
            tuples.add(createTuple("book:101", 0.85));
            tuples.add(createTuple("book:102", 0.75));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            List<RecommendationResult> results = cacheService.getRecommendations(userId, limit);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results.get(0).getBookId()).isEqualTo(100L);
            assertThat(results.get(0).getScore()).isEqualTo(0.95);
            assertThat(results.get(0).getRank()).isEqualTo(1);
        }

        @Test
        @DisplayName("빈 캐시는 빈 리스트를 반환한다")
        void get_EmptyCache() {
            // Given
            Long userId = 1L;
            when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .thenReturn(null);

            // When
            List<RecommendationResult> results = cacheService.getRecommendations(userId, 10);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("cursor 기반 페이징이 동작한다")
        void get_WithCursor() {
            // Given
            Long userId = 1L;
            Long cursor = 100L;
            int limit = 2;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("book:100", 0.95));
            tuples.add(createTuple("book:101", 0.85));
            tuples.add(createTuple("book:102", 0.75));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            List<RecommendationResult> results = cacheService.getRecommendations(userId, cursor, limit);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getBookId()).isEqualTo(101L);
            assertThat(results.get(1).getBookId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("잘못된 member 형식은 무시한다")
        void get_IgnoresInvalidMember() {
            // Given
            Long userId = 1L;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("book:100", 0.95));
            tuples.add(createTuple("invalid:200", 0.85)); // should be ignored
            tuples.add(createTuple("book:102", 0.75));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            List<RecommendationResult> results = cacheService.getRecommendations(userId, 10);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getBookId()).isEqualTo(100L);
            assertThat(results.get(1).getBookId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("예외 발생시 빈 리스트를 반환한다")
        void get_ReturnsEmptyOnException() {
            // Given
            Long userId = 1L;
            when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Redis error"));

            // When
            List<RecommendationResult> results = cacheService.getRecommendations(userId, 10);

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRecommendationsWithSampling 테스트")
    class GetRecommendationsWithSamplingTest {

        @Test
        @DisplayName("샘플링 활성화시 WindowSampler를 사용한다")
        void withSampling_UsesWindowSampler() {
            // Given
            Long userId = 1L;
            String sessionId = "session-123";

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("book:100", 0.95));
            tuples.add(createTuple("book:101", 0.85));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            List<RecommendationResult> sampledResults = List.of(
                    RecommendationResult.builder().bookId(101L).score(0.85).build(),
                    RecommendationResult.builder().bookId(100L).score(0.95).build()
            );
            when(windowSampler.applySampling(anyList(), eq(sessionId))).thenReturn(sampledResults);

            // When
            List<RecommendationResult> results = cacheService.getRecommendationsWithSampling(
                    userId, null, 10, sessionId, true
            );

            // Then
            verify(windowSampler).applySampling(anyList(), eq(sessionId));
            assertThat(results).hasSize(2);
        }

        @Test
        @DisplayName("샘플링 비활성화시 WindowSampler를 사용하지 않는다")
        void withoutSampling_SkipsWindowSampler() {
            // Given
            Long userId = 1L;

            Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
            tuples.add(createTuple("book:100", 0.95));

            when(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(-1L)))
                    .thenReturn(tuples);

            // When
            cacheService.getRecommendationsWithSampling(userId, null, 10, null, false);

            // Then
            verify(windowSampler, never()).applySampling(anyList(), anyString());
        }

        @Test
        @DisplayName("빈 캐시는 빈 리스트를 반환한다")
        void withSampling_EmptyCache() {
            // Given
            Long userId = 1L;
            when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong()))
                    .thenReturn(null);

            // When
            List<RecommendationResult> results = cacheService.getRecommendationsWithSampling(
                    userId, null, 10, "session", true
            );

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("getBookScore 테스트")
    class GetBookScoreTest {

        @Test
        @DisplayName("특정 도서의 점수를 조회한다")
        void getScore_Success() {
            // Given
            Long userId = 1L;
            Long bookId = 100L;
            when(zSetOperations.score("recommend:user:1", "book:100")).thenReturn(0.95);

            // When
            Double score = cacheService.getBookScore(userId, bookId);

            // Then
            assertThat(score).isEqualTo(0.95);
        }

        @Test
        @DisplayName("존재하지 않는 도서는 null을 반환한다")
        void getScore_NotFound() {
            // Given
            Long userId = 1L;
            Long bookId = 999L;
            when(zSetOperations.score(anyString(), anyString())).thenReturn(null);

            // When
            Double score = cacheService.getBookScore(userId, bookId);

            // Then
            assertThat(score).isNull();
        }

        @Test
        @DisplayName("예외 발생시 null을 반환한다")
        void getScore_ReturnsNullOnException() {
            // Given
            Long userId = 1L;
            Long bookId = 100L;
            when(zSetOperations.score(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Redis error"));

            // When
            Double score = cacheService.getBookScore(userId, bookId);

            // Then
            assertThat(score).isNull();
        }
    }

    @Nested
    @DisplayName("incrementBookScore 테스트")
    class IncrementBookScoreTest {

        @Test
        @DisplayName("점수를 증분한다")
        void increment_Success() {
            // Given
            Long userId = 1L;
            Long bookId = 100L;
            double delta = 0.1;

            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getTtlHours()).thenReturn(3);
            when(zSetOperations.size(anyString())).thenReturn(50L);

            // When
            cacheService.incrementBookScore(userId, bookId, delta);

            // Then
            verify(zSetOperations).incrementScore("recommend:user:1", "book:100", delta);
            verify(redisTemplate).expire(eq("recommend:user:1"), eq(3L), eq(TimeUnit.HOURS));
        }

        @Test
        @DisplayName("null userId는 아무 작업도 하지 않는다")
        void increment_NullUserId() {
            // When
            cacheService.incrementBookScore(null, 100L, 0.1);

            // Then
            verify(zSetOperations, never()).incrementScore(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("null bookId는 아무 작업도 하지 않는다")
        void increment_NullBookId() {
            // When
            cacheService.incrementBookScore(1L, null, 0.1);

            // Then
            verify(zSetOperations, never()).incrementScore(anyString(), anyString(), anyDouble());
        }

        @Test
        @DisplayName("100개 초과시 하위 항목을 제거한다")
        void increment_TrimsExcess() {
            // Given
            when(properties.getCache()).thenReturn(cacheConfig);
            when(cacheConfig.getTtlHours()).thenReturn(3);
            when(zSetOperations.size(anyString())).thenReturn(150L);

            // When
            cacheService.incrementBookScore(1L, 100L, 0.1);

            // Then
            verify(zSetOperations).removeRange(eq("recommend:user:1"), eq(0L), eq(49L));
        }
    }

    @Nested
    @DisplayName("clearRecommendations 테스트")
    class ClearRecommendationsTest {

        @Test
        @DisplayName("캐시를 삭제한다")
        void clear_Success() {
            // Given
            Long userId = 1L;

            // When
            cacheService.clearRecommendations(userId);

            // Then
            verify(redisTemplate).delete("recommend:user:1");
        }
    }

    @Nested
    @DisplayName("hasCachedRecommendations 테스트")
    class HasCachedRecommendationsTest {

        @Test
        @DisplayName("캐시가 존재하면 true를 반환한다")
        void hasCache_True() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:user:1")).thenReturn(10L);

            // When
            boolean result = cacheService.hasCachedRecommendations(userId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("캐시가 비어있으면 false를 반환한다")
        void hasCache_False_Empty() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:user:1")).thenReturn(0L);

            // When
            boolean result = cacheService.hasCachedRecommendations(userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("캐시가 null이면 false를 반환한다")
        void hasCache_False_Null() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:user:1")).thenReturn(null);

            // When
            boolean result = cacheService.hasCachedRecommendations(userId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getCacheStats 테스트")
    class GetCacheStatsTest {

        @Test
        @DisplayName("캐시 통계를 반환한다")
        void getStats_Success() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:user:1")).thenReturn(50L);
            when(redisTemplate.getExpire("recommend:user:1", TimeUnit.SECONDS)).thenReturn(3600L);

            // When
            RecommendationCacheService.CacheStats stats = cacheService.getCacheStats(userId);

            // Then
            assertThat(stats.getUserId()).isEqualTo(userId);
            assertThat(stats.getCachedItems()).isEqualTo(50L);
            assertThat(stats.getTtlSeconds()).isEqualTo(3600L);
            assertThat(stats.isExists()).isTrue();
        }

        @Test
        @DisplayName("빈 캐시의 통계를 반환한다")
        void getStats_EmptyCache() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:user:1")).thenReturn(0L);
            when(redisTemplate.getExpire("recommend:user:1", TimeUnit.SECONDS)).thenReturn(-2L);

            // When
            RecommendationCacheService.CacheStats stats = cacheService.getCacheStats(userId);

            // Then
            assertThat(stats.getCachedItems()).isEqualTo(0L);
            assertThat(stats.isExists()).isFalse();
        }

        @Test
        @DisplayName("null 값은 0으로 처리한다")
        void getStats_NullValues() {
            // Given
            Long userId = 1L;
            when(zSetOperations.size("recommend:user:1")).thenReturn(null);
            when(redisTemplate.getExpire("recommend:user:1", TimeUnit.SECONDS)).thenReturn(null);

            // When
            RecommendationCacheService.CacheStats stats = cacheService.getCacheStats(userId);

            // Then
            assertThat(stats.getCachedItems()).isEqualTo(0L);
            assertThat(stats.getTtlSeconds()).isEqualTo(0L);
            assertThat(stats.isExists()).isFalse();
        }
    }

    @Nested
    @DisplayName("CacheStats 테스트")
    class CacheStatsTest {

        @Test
        @DisplayName("Builder로 CacheStats를 생성할 수 있다")
        void builder_Works() {
            // When
            RecommendationCacheService.CacheStats stats = RecommendationCacheService.CacheStats.builder()
                    .userId(1L)
                    .cachedItems(100L)
                    .ttlSeconds(3600L)
                    .exists(true)
                    .build();

            // Then
            assertThat(stats.getUserId()).isEqualTo(1L);
            assertThat(stats.getCachedItems()).isEqualTo(100L);
            assertThat(stats.getTtlSeconds()).isEqualTo(3600L);
            assertThat(stats.isExists()).isTrue();
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
