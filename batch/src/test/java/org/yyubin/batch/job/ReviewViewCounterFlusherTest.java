package org.yyubin.batch.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;
import org.yyubin.application.review.port.ReviewViewFlushPort;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewViewCounterFlusher 테스트")
class ReviewViewCounterFlusherTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ReviewViewFlushPort reviewViewFlushPort;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private Cursor<byte[]> cursor;

    @InjectMocks
    private ReviewViewCounterFlusher reviewViewCounterFlusher;

    @Test
    @DisplayName("조회수 카운터 플러시 성공")
    void flush_Success() {
        // Given
        setupRedisMocks();
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next())
                .thenReturn("metric:review:view:1".getBytes())
                .thenReturn("metric:review:view:2".getBytes());
        when(valueOperations.get("metric:review:view:1")).thenReturn("10");
        when(valueOperations.get("metric:review:view:2")).thenReturn("5");

        // When
        reviewViewCounterFlusher.flush();

        // Then
        verify(redisTemplate).delete("metric:review:view:1");
        verify(redisTemplate).delete("metric:review:view:2");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReviewViewFlushPort.CounterUpdate>> dbCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(reviewViewFlushPort).batchUpdateViewCount(dbCaptor.capture());
        assertThat(dbCaptor.getValue()).hasSize(2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Long, Long>> esCaptor = ArgumentCaptor.forClass(Map.class);
        verify(reviewViewFlushPort).updateSearchIndexViewCount(esCaptor.capture());
        assertThat(esCaptor.getValue()).hasSize(2);
    }

    @Test
    @DisplayName("플러시할 카운터가 없는 경우")
    void flush_NoCounters() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);

        // When
        reviewViewCounterFlusher.flush();

        // Then
        verify(reviewViewFlushPort, never()).batchUpdateViewCount(any());
        verify(reviewViewFlushPort, never()).updateSearchIndexViewCount(any());
    }

    @Test
    @DisplayName("0 이하의 값은 무시")
    void flush_IgnoresZeroOrNegativeValues() {
        // Given
        setupRedisMocks();
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next())
                .thenReturn("metric:review:view:1".getBytes())
                .thenReturn("metric:review:view:2".getBytes());
        when(valueOperations.get("metric:review:view:1")).thenReturn("0");
        when(valueOperations.get("metric:review:view:2")).thenReturn("-5");

        // When
        reviewViewCounterFlusher.flush();

        // Then
        verify(redisTemplate, never()).delete(anyString());
        verify(reviewViewFlushPort, never()).batchUpdateViewCount(any());
    }

    @Test
    @DisplayName("null 값은 무시")
    void flush_IgnoresNullValues() {
        // Given
        setupRedisMocks();
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn("metric:review:view:1".getBytes());
        when(valueOperations.get("metric:review:view:1")).thenReturn(null);

        // When
        reviewViewCounterFlusher.flush();

        // Then
        verify(redisTemplate, never()).delete(anyString());
        verify(reviewViewFlushPort, never()).batchUpdateViewCount(any());
    }

    @Test
    @DisplayName("잘못된 키 형식은 무시")
    void flush_IgnoresInvalidKeys() {
        // Given
        setupRedisMocks();
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn("metric:review:view:invalid".getBytes());
        when(valueOperations.get("metric:review:view:invalid")).thenReturn("10");

        // When
        reviewViewCounterFlusher.flush();

        // Then
        // 유효하지 않은 reviewId이므로 삭제만 되고 업데이트는 안됨
        verify(redisTemplate).delete("metric:review:view:invalid");
    }

    @Test
    @DisplayName("스캔 중 예외 발생 시 빈 리스트 반환")
    void flush_ScanException() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.scan(any(ScanOptions.class))).thenThrow(new RuntimeException("Scan failed"));

        // When
        reviewViewCounterFlusher.flush();

        // Then
        verify(reviewViewFlushPort, never()).batchUpdateViewCount(any());
    }

    @Test
    @DisplayName("숫자가 아닌 값은 무시")
    void flush_IgnoresNonNumericValues() {
        // Given
        setupRedisMocks();
        when(cursor.hasNext()).thenReturn(true, false);
        when(cursor.next()).thenReturn("metric:review:view:1".getBytes());
        when(valueOperations.get("metric:review:view:1")).thenReturn("not_a_number");

        // When
        reviewViewCounterFlusher.flush();

        // Then
        verify(redisTemplate, never()).delete(anyString());
        verify(reviewViewFlushPort, never()).batchUpdateViewCount(any());
    }

    private void setupRedisMocks() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
}
