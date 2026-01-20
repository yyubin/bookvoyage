package org.yyubin.infrastructure.stream.metric;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.yyubin.application.review.port.ReviewViewFlushPort;
import org.yyubin.infrastructure.config.MetricProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReviewViewMetricAdapter 테스트")
class ReviewViewMetricAdapterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private MetricProperties metricProperties;

    @Mock
    private ReviewViewFlushPort reviewViewFlushPort;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private ReviewViewMetricAdapter adapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(metricProperties.getDedupTtlSeconds()).thenReturn(3600L);
        when(metricProperties.getCounterTtlSeconds()).thenReturn(3600L);
    }

    @Test
    @DisplayName("reviewId가 null이면 0을 반환한다")
    void incrementAndGet_NullReviewId_ReturnsZero() {
        // When
        long result = adapter.incrementAndGet(null, 1L);

        // Then
        assertThat(result).isEqualTo(0L);
        verify(redisTemplate, never()).opsForSet();
        verify(redisTemplate, never()).opsForValue();
    }

    @Test
    @DisplayName("이미 본 사용자면 캐시/DB 값으로 반환한다")
    void incrementAndGet_Dedup_ReturnsExistingCount() {
        // Given
        when(setOperations.add("metric:review:viewdedup:1", "2")).thenReturn(0L);
        when(valueOperations.get("metric:review:view:1")).thenReturn("7");

        // When
        long result = adapter.incrementAndGet(1L, 2L);

        // Then
        assertThat(result).isEqualTo(7L);
        verify(valueOperations, never()).increment("metric:review:view:1");
        verify(redisTemplate).expire(eq("metric:review:viewdedup:1"), any(Duration.class));
    }

    @Test
    @DisplayName("카운터 초기화 후 증가시킨다")
    void incrementAndGet_InitializesAndIncrements() {
        // Given
        when(setOperations.add("metric:review:viewdedup:1", "2")).thenReturn(1L);
        when(valueOperations.get("metric:review:view:1")).thenReturn(null);
        when(reviewViewFlushPort.findCurrentViewCount(1L)).thenReturn(Optional.of(5L));
        when(valueOperations.increment("metric:review:view:1")).thenReturn(6L);

        // When
        long result = adapter.incrementAndGet(1L, 2L);

        // Then
        assertThat(result).isEqualTo(6L);
        verify(valueOperations).setIfAbsent(eq("metric:review:view:1"), eq("5"), any(Duration.class));
        verify(redisTemplate).expire(eq("metric:review:view:1"), any(Duration.class));
    }

    @Test
    @DisplayName("배치 조회는 캐시 미스만 DB 폴백을 사용한다")
    void getBatchCountsWithFallback_UsesFallbackForMisses() {
        // Given
        when(valueOperations.get("metric:review:view:1")).thenReturn("10");
        when(valueOperations.get("metric:review:view:2")).thenReturn(null);
        when(reviewViewFlushPort.findCurrentViewCount(2L)).thenReturn(Optional.of(20L));

        // When
        Map<Long, Long> result = adapter.getBatchCountsWithFallback(List.of(1L, 2L));

        // Then
        assertThat(result).containsEntry(1L, 10L);
        assertThat(result).containsEntry(2L, 20L);
        verify(reviewViewFlushPort).findCurrentViewCount(2L);
        verify(reviewViewFlushPort, never()).findCurrentViewCount(1L);
    }
}
