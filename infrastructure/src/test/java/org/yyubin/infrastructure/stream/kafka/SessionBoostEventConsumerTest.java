package org.yyubin.infrastructure.stream.kafka;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.yyubin.application.event.EventPayload;
import org.yyubin.infrastructure.config.SessionBoostProperties;
import org.yyubin.infrastructure.stream.metric.ReviewTrackingCounterAdapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionBoostEventConsumer 테스트")
class SessionBoostEventConsumerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    private final SessionBoostProperties properties = new SessionBoostProperties();

    @Mock
    private ReviewTrackingCounterAdapter reviewTrackingCounterAdapter;

    @InjectMocks
    private SessionBoostEventConsumer consumer;

    @Test
    @DisplayName("userId가 null이면 아무 작업도 하지 않는다")
    void consume_NullUserId_NoOp() {
        // Given
        EventPayload payload = new EventPayload(null, "REVIEW_CLICKED", null, null, null, Map.of("reviewId", 1L), null, null, 1);

        // When
        consumer.consume(payload);

        // Then
        verify(redisTemplate, never()).executePipelined(any(RedisCallback.class));
        verify(reviewTrackingCounterAdapter, never()).incrementClick(any());
    }

    @Test
    @DisplayName("reviewId가 없으면 파이프라인을 실행하지 않는다")
    void consume_NoReviewId_NoOp() {
        // Given
        EventPayload payload = new EventPayload(null, "REVIEW_CLICKED", 1L, null, null, Map.of(), null, null, 1);

        // When
        consumer.consume(payload);

        // Then
        verify(redisTemplate, never()).executePipelined(any(RedisCallback.class));
        verify(reviewTrackingCounterAdapter, never()).incrementClick(any());
    }

    @Test
    @DisplayName("REVIEW_CLICKED 이벤트는 클릭 카운트를 증가시킨다")
    void consume_Click_IncrementsCounter() {
        // Given
        EventPayload payload = new EventPayload(null, "REVIEW_CLICKED", 1L, null, null, Map.of("reviewId", 10L), null, null, 1);

        // When
        consumer.consume(payload);

        // Then
        verify(reviewTrackingCounterAdapter).incrementClick(10L);
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    @DisplayName("REVIEW_DWELL 이벤트는 체류 시간을 누적한다")
    void consume_Dwell_AddsDwell() {
        // Given
        EventPayload payload = new EventPayload(
                null,
                "REVIEW_DWELL",
                1L,
                null,
                null,
                Map.of("reviewId", 10L, "dwellMs", 5000L),
                null,
                null,
                1
        );

        // When
        consumer.consume(payload);

        // Then
        verify(reviewTrackingCounterAdapter).addDwell(10L, 5000L);
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }

    @Test
    @DisplayName("REVIEW_REACHED 이벤트는 도달 카운트를 증가시킨다")
    void consume_Reached_IncrementsReach() {
        // Given
        EventPayload payload = new EventPayload(null, "REVIEW_REACHED", 1L, null, null, Map.of("reviewId", 11L), null, null, 1);

        // When
        consumer.consume(payload);

        // Then
        verify(reviewTrackingCounterAdapter).incrementReach(11L);
        verify(redisTemplate).executePipelined(any(RedisCallback.class));
    }
}
