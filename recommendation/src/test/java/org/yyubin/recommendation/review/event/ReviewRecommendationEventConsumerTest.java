package org.yyubin.recommendation.review.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRecommendationEventConsumer 테스트")
class ReviewRecommendationEventConsumerTest {

    @Mock
    private ReviewRecommendationEventHandler handler;

    @InjectMocks
    private ReviewRecommendationEventConsumer consumer;

    @Test
    @DisplayName("null 이벤트는 핸들러 호출하지 않음")
    void consume_NullEvent_NoHandlerCall() {
        // When
        consumer.consume(null);

        // Then
        verifyNoInteractions(handler);
    }

    @Test
    @DisplayName("정상 이벤트는 핸들러에 전달")
    void consume_ValidEvent_CallsHandler() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                1L, 2L, 3L, "title", "summary", "content",
                List.of("highlight"), List.of("norm"), List.of("keyword"),
                "genre", LocalDateTime.now(), 5
        );

        // When
        consumer.consume(event);

        // Then
        verify(handler, times(1)).handle(event);
    }

    @Test
    @DisplayName("DELETE 이벤트도 핸들러에 전달")
    void consume_DeleteEvent_CallsHandler() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.DELETE,
                100L, null, null, null, null, null,
                null, null, null, null, null, null
        );

        // When
        consumer.consume(event);

        // Then
        verify(handler).handle(event);
    }

    @Test
    @DisplayName("UPSERT 이벤트 핸들러에 전달")
    void consume_UpsertEvent_CallsHandler() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                1L, 2L, 3L, "Book Title", "Summary", "Content",
                List.of("highlight1", "highlight2"),
                List.of("norm1", "norm2"),
                List.of("keyword1"),
                "Fiction",
                LocalDateTime.of(2024, 1, 15, 10, 30),
                4
        );

        // When
        consumer.consume(event);

        // Then
        verify(handler).handle(event);
    }

    @Test
    @DisplayName("빈 리스트를 가진 이벤트도 정상 전달")
    void consume_EventWithEmptyLists_CallsHandler() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                1L, 2L, 3L, "title", "summary", "content",
                List.of(), List.of(), List.of(),
                "genre", LocalDateTime.now(), 3
        );

        // When
        consumer.consume(event);

        // Then
        verify(handler).handle(event);
    }

    @Test
    @DisplayName("null 필드를 가진 이벤트도 정상 전달")
    void consume_EventWithNullFields_CallsHandler() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                1L, 2L, 3L, null, null, null,
                null, null, null, null, null, null
        );

        // When
        consumer.consume(event);

        // Then
        verify(handler).handle(event);
    }

    @Test
    @DisplayName("연속 이벤트 처리")
    void consume_MultipleEvents_CallsHandlerMultipleTimes() {
        // Given
        ReviewSearchIndexEvent event1 = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                1L, 2L, 3L, "title1", "summary1", "content1",
                List.of(), List.of(), List.of(), "genre1", LocalDateTime.now(), 5
        );
        ReviewSearchIndexEvent event2 = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                2L, 3L, 4L, "title2", "summary2", "content2",
                List.of(), List.of(), List.of(), "genre2", LocalDateTime.now(), 4
        );
        ReviewSearchIndexEvent event3 = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.DELETE,
                3L, null, null, null, null, null,
                null, null, null, null, null, null
        );

        // When
        consumer.consume(event1);
        consumer.consume(event2);
        consumer.consume(event3);

        // Then
        verify(handler, times(3)).handle(any());
        verify(handler).handle(event1);
        verify(handler).handle(event2);
        verify(handler).handle(event3);
    }

    @Test
    @DisplayName("핸들러 예외 발생 시 전파")
    void consume_HandlerThrowsException_Propagates() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT,
                1L, 2L, 3L, "title", "summary", "content",
                List.of(), List.of(), List.of(), "genre", LocalDateTime.now(), 5
        );
        doThrow(new RuntimeException("Handler error")).when(handler).handle(event);

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> consumer.consume(event));

        verify(handler).handle(event);
    }
}
