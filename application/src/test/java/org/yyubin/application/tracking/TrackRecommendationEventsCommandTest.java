package org.yyubin.application.tracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.application.tracking.command.TrackRecommendationEventsCommand;
import org.yyubin.application.tracking.command.TrackingEventCommand;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TrackRecommendationEventsCommand 테스트")
class TrackRecommendationEventsCommandTest {

    @Test
    @DisplayName("정상적인 이벤트 목록으로 생성")
    void create_WithValidEvents() {
        // Given
        TrackingEventCommand event = createTrackingEvent("event-123", "IMPRESSION", "REVIEW", "100");

        // When
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // Then
        assertThat(command.events()).hasSize(1);
        assertThat(command.events().get(0)).isEqualTo(event);
    }

    @Test
    @DisplayName("여러 이벤트로 생성")
    void create_WithMultipleEvents() {
        // Given
        TrackingEventCommand event1 = createTrackingEvent("event-1", "IMPRESSION", "REVIEW", "100");
        TrackingEventCommand event2 = createTrackingEvent("event-2", "CLICK", "BOOK", "200");
        TrackingEventCommand event3 = createTrackingEvent("event-3", "DWELL", "USER", "300");

        // When
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event1, event2, event3));

        // Then
        assertThat(command.events()).hasSize(3);
    }

    @Test
    @DisplayName("null 이벤트 목록으로 생성 시 예외")
    void create_WithNullEvents_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> new TrackRecommendationEventsCommand(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("events must not be empty");
    }

    @Test
    @DisplayName("빈 이벤트 목록으로 생성 시 예외")
    void create_WithEmptyEvents_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> new TrackRecommendationEventsCommand(Collections.emptyList()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("events must not be empty");
    }

    private TrackingEventCommand createTrackingEvent(String eventId, String eventType, String contentType, String contentId) {
        return new TrackingEventCommand(
                eventId,
                eventType,
                1L,
                "session-456",
                "device-789",
                "2024-01-01T10:00:00Z",
                "recommendation",
                contentType,
                contentId,
                0,
                1,
                0.95,
                "request-abc",
                "hybrid-v1",
                5000L,
                75.5,
                3000L,
                null
        );
    }
}
