package org.yyubin.recommendation.tracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPayload;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationTrackingEventConsumer 테스트")
class RecommendationTrackingEventConsumerTest {

    @Mock
    private RecommendationTrackingEventHandler handler;

    @InjectMocks
    private RecommendationTrackingEventConsumer consumer;

    @Test
    @DisplayName("유효한 payload를 수신하면 handler에 전달한다")
    void consume_ValidPayload_DelegatesToHandler() {
        // Given
        EventPayload payload = new EventPayload(
                UUID.randomUUID(),
                "CLICK",
                1L,
                "BOOK",
                "100",
                Map.of("contentType", "BOOK", "contentId", "100"),
                Instant.now(),
                "web",
                1
        );

        // When
        consumer.consume(payload);

        // Then
        verify(handler, times(1)).handle(payload);
    }

    @Test
    @DisplayName("null payload를 수신하면 handler를 호출하지 않는다")
    void consume_NullPayload_DoesNotDelegateToHandler() {
        // When
        consumer.consume(null);

        // Then
        verify(handler, never()).handle(any());
    }

    @Test
    @DisplayName("여러 이벤트를 연속으로 수신해도 각각 handler에 전달한다")
    void consume_MultiplePayloads_DelegatesToHandlerMultipleTimes() {
        // Given
        EventPayload payload1 = new EventPayload(
                UUID.randomUUID(),
                "CLICK",
                1L,
                "BOOK",
                "100",
                null,
                Instant.now(),
                "web",
                1
        );
        EventPayload payload2 = new EventPayload(
                UUID.randomUUID(),
                "BOOKMARK",
                2L,
                "BOOK",
                "200",
                null,
                Instant.now(),
                "mobile",
                1
        );

        // When
        consumer.consume(payload1);
        consumer.consume(payload2);

        // Then
        verify(handler, times(1)).handle(payload1);
        verify(handler, times(1)).handle(payload2);
        verify(handler, times(2)).handle(any());
    }

    @Test
    @DisplayName("handler에서 예외가 발생해도 consumer는 예외를 전파한다")
    void consume_HandlerThrowsException_PropagatesException() {
        // Given
        EventPayload payload = new EventPayload(
                UUID.randomUUID(),
                "CLICK",
                1L,
                "BOOK",
                "100",
                null,
                Instant.now(),
                "web",
                1
        );
        doThrow(new RuntimeException("Handler error")).when(handler).handle(payload);

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            consumer.consume(payload);
        });
    }
}
