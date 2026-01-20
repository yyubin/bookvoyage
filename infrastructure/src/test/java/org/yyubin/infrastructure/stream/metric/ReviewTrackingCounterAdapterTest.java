package org.yyubin.infrastructure.stream.metric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewTrackingCounterAdapter 테스트")
class ReviewTrackingCounterAdapterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ReviewTrackingCounterAdapter adapter;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("노출 카운트를 증가시킨다")
    void incrementImpression_IncrementsKey() {
        // When
        adapter.incrementImpression(10L);

        // Then
        verify(valueOperations).increment("metric:review:impression:10");
    }

    @Test
    @DisplayName("도달 카운트를 증가시킨다")
    void incrementReach_IncrementsKey() {
        // When
        adapter.incrementReach(11L);

        // Then
        verify(valueOperations).increment("metric:review:reach:11");
    }

    @Test
    @DisplayName("클릭 카운트를 증가시킨다")
    void incrementClick_IncrementsKey() {
        // When
        adapter.incrementClick(12L);

        // Then
        verify(valueOperations).increment("metric:review:click:12");
    }

    @Test
    @DisplayName("체류 시간 카운트를 누적한다")
    void addDwell_IncrementsKeys() {
        // When
        adapter.addDwell(13L, 5000L);

        // Then
        verify(valueOperations).increment("metric:review:dwell:sum:13", 5000L);
        verify(valueOperations).increment("metric:review:dwell:count:13");
    }
}
