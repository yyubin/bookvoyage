package org.yyubin.batch.sync;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewEngagementStatsProvider 테스트")
class ReviewEngagementStatsProviderTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ReviewEngagementStatsProvider provider;

    @BeforeEach
    void setUp() {
        provider = new ReviewEngagementStatsProvider(redisTemplate);
    }

    @Test
    @DisplayName("정상적인 통계 조회")
    void getStats_Success() {
        // Given
        Long reviewId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("metric:review:impression:1")).thenReturn("1000");
        when(valueOperations.get("metric:review:reach:1")).thenReturn("800");
        when(valueOperations.get("metric:review:click:1")).thenReturn("100");
        when(valueOperations.get("metric:review:dwell:sum:1")).thenReturn("50000");
        when(valueOperations.get("metric:review:dwell:count:1")).thenReturn("50");

        // When
        ReviewEngagementStats stats = provider.getStats(reviewId);

        // Then
        assertThat(stats.impressions()).isEqualTo(1000);
        assertThat(stats.reaches()).isEqualTo(800);
        assertThat(stats.clicks()).isEqualTo(100);
        assertThat(stats.totalDwellMs()).isEqualTo(50000);
        assertThat(stats.dwellCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("reviewId가 null인 경우")
    void getStats_NullReviewId() {
        // When
        ReviewEngagementStats stats = provider.getStats(null);

        // Then
        assertThat(stats.impressions()).isEqualTo(0);
        assertThat(stats.reaches()).isEqualTo(0);
        assertThat(stats.clicks()).isEqualTo(0);
        assertThat(stats.totalDwellMs()).isEqualTo(0);
        assertThat(stats.dwellCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Redis에 값이 없는 경우")
    void getStats_NoValues() {
        // Given
        Long reviewId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("metric:review:impression:1")).thenReturn(null);
        when(valueOperations.get("metric:review:reach:1")).thenReturn(null);
        when(valueOperations.get("metric:review:click:1")).thenReturn(null);
        when(valueOperations.get("metric:review:dwell:sum:1")).thenReturn(null);
        when(valueOperations.get("metric:review:dwell:count:1")).thenReturn(null);

        // When
        ReviewEngagementStats stats = provider.getStats(reviewId);

        // Then
        assertThat(stats.impressions()).isEqualTo(0);
        assertThat(stats.reaches()).isEqualTo(0);
        assertThat(stats.clicks()).isEqualTo(0);
        assertThat(stats.totalDwellMs()).isEqualTo(0);
        assertThat(stats.dwellCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("잘못된 숫자 형식인 경우")
    void getStats_InvalidNumberFormat() {
        // Given
        Long reviewId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("metric:review:impression:1")).thenReturn("invalid");
        when(valueOperations.get("metric:review:reach:1")).thenReturn("not_a_number");
        when(valueOperations.get("metric:review:click:1")).thenReturn("abc");
        when(valueOperations.get("metric:review:dwell:sum:1")).thenReturn("xyz");
        when(valueOperations.get("metric:review:dwell:count:1")).thenReturn("123abc");

        // When
        ReviewEngagementStats stats = provider.getStats(reviewId);

        // Then
        assertThat(stats.impressions()).isEqualTo(0);
        assertThat(stats.reaches()).isEqualTo(0);
        assertThat(stats.clicks()).isEqualTo(0);
        assertThat(stats.totalDwellMs()).isEqualTo(0);
        assertThat(stats.dwellCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("일부 값만 있는 경우")
    void getStats_PartialValues() {
        // Given
        Long reviewId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("metric:review:impression:1")).thenReturn("500");
        when(valueOperations.get("metric:review:reach:1")).thenReturn(null);
        when(valueOperations.get("metric:review:click:1")).thenReturn("50");
        when(valueOperations.get("metric:review:dwell:sum:1")).thenReturn(null);
        when(valueOperations.get("metric:review:dwell:count:1")).thenReturn("10");

        // When
        ReviewEngagementStats stats = provider.getStats(reviewId);

        // Then
        assertThat(stats.impressions()).isEqualTo(500);
        assertThat(stats.reaches()).isEqualTo(0);
        assertThat(stats.clicks()).isEqualTo(50);
        assertThat(stats.totalDwellMs()).isEqualTo(0);
        assertThat(stats.dwellCount()).isEqualTo(10);
    }

    @Test
    @DisplayName("큰 숫자 처리")
    void getStats_LargeNumbers() {
        // Given
        Long reviewId = 1L;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("metric:review:impression:1")).thenReturn(String.valueOf(Long.MAX_VALUE));
        when(valueOperations.get("metric:review:reach:1")).thenReturn("9999999999999");
        when(valueOperations.get("metric:review:click:1")).thenReturn("1234567890123");
        when(valueOperations.get("metric:review:dwell:sum:1")).thenReturn("9876543210987");
        when(valueOperations.get("metric:review:dwell:count:1")).thenReturn("1111111111111");

        // When
        ReviewEngagementStats stats = provider.getStats(reviewId);

        // Then
        assertThat(stats.impressions()).isEqualTo(Long.MAX_VALUE);
        assertThat(stats.reaches()).isEqualTo(9999999999999L);
        assertThat(stats.clicks()).isEqualTo(1234567890123L);
        assertThat(stats.totalDwellMs()).isEqualTo(9876543210987L);
        assertThat(stats.dwellCount()).isEqualTo(1111111111111L);
    }
}
