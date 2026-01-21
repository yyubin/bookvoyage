package org.yyubin.application.tracking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.tracking.command.TrackRecommendationEventsCommand;
import org.yyubin.application.tracking.command.TrackingEventCommand;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationTrackingService 테스트")
class RecommendationTrackingServiceTest {

    @Mock
    private EventPublisher eventPublisher;

    private RecommendationTrackingService service;

    @BeforeEach
    void setUp() {
        service = new RecommendationTrackingService(eventPublisher);
    }

    @Test
    @DisplayName("정상적인 이벤트 트래킹")
    void track_Success() {
        // Given
        TrackingEventCommand event = createValidEvent();
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When
        service.track(command);

        // Then
        ArgumentCaptor<EventPayload> payloadCaptor = ArgumentCaptor.forClass(EventPayload.class);
        verify(eventPublisher).publish(eq(EventTopics.TRACKING), eq("1"), payloadCaptor.capture());

        EventPayload payload = payloadCaptor.getValue();
        assertThat(payload.eventType()).isEqualTo("IMPRESSION");
        assertThat(payload.userId()).isEqualTo(1L);
        assertThat(payload.targetType()).isEqualTo("REVIEW");
        assertThat(payload.targetId()).isEqualTo("100");
        assertThat(payload.source()).isEqualTo("tracking-api");
        assertThat(payload.schemaVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 이벤트 트래킹")
    void track_MultipleEvents() {
        // Given
        TrackingEventCommand event1 = createEventWithParams("event-1", "IMPRESSION", "REVIEW", "100", 1L);
        TrackingEventCommand event2 = createEventWithParams("event-2", "CLICK", "BOOK", "200", 2L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event1, event2));

        // When
        service.track(command);

        // Then
        verify(eventPublisher, times(2)).publish(eq(EventTopics.TRACKING), any(), any());
    }

    @Test
    @DisplayName("eventId가 null인 경우 예외")
    void track_NullEventId_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams(null, "IMPRESSION", "REVIEW", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("eventId is required");
    }

    @Test
    @DisplayName("eventId가 빈 문자열인 경우 예외")
    void track_BlankEventId_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams("  ", "IMPRESSION", "REVIEW", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("eventId is required");
    }

    @Test
    @DisplayName("eventType이 null인 경우 예외")
    void track_NullEventType_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams("event-1", null, "REVIEW", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("eventType is required");
    }

    @Test
    @DisplayName("contentType이 null인 경우 예외")
    void track_NullContentType_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams("event-1", "IMPRESSION", null, "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("contentType is required");
    }

    @Test
    @DisplayName("contentId가 null인 경우 예외")
    void track_NullContentId_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams("event-1", "IMPRESSION", "REVIEW", null, 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("contentId is required");
    }

    @Test
    @DisplayName("지원하지 않는 eventType인 경우 예외")
    void track_UnsupportedEventType_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams("event-1", "INVALID_TYPE", "REVIEW", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported eventType: INVALID_TYPE");
    }

    @Test
    @DisplayName("지원하지 않는 contentType인 경우 예외")
    void track_UnsupportedContentType_ThrowsException() {
        // Given
        TrackingEventCommand event = createEventWithParams("event-1", "IMPRESSION", "INVALID_TYPE", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When & Then
        assertThatThrownBy(() -> service.track(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported contentType: INVALID_TYPE");
    }

    @Test
    @DisplayName("userId가 null인 경우 sessionId를 key로 사용")
    void track_NullUserId_UsesSessionId() {
        // Given
        TrackingEventCommand event = new TrackingEventCommand(
                "event-1", "IMPRESSION", null, "session-123", null, null,
                null, "REVIEW", "100", null, null, null, null, null, null, null, null, null
        );
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When
        service.track(command);

        // Then
        verify(eventPublisher).publish(eq(EventTopics.TRACKING), eq("session-123"), any());
    }

    @Test
    @DisplayName("userId와 sessionId가 모두 null인 경우 anonymous를 key로 사용")
    void track_NullUserIdAndSessionId_UsesAnonymous() {
        // Given
        TrackingEventCommand event = new TrackingEventCommand(
                "event-1", "IMPRESSION", null, null, null, null,
                null, "REVIEW", "100", null, null, null, null, null, null, null, null, null
        );
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When
        service.track(command);

        // Then
        verify(eventPublisher).publish(eq(EventTopics.TRACKING), eq("anonymous"), any());
    }

    @Test
    @DisplayName("메타데이터 포함하여 트래킹")
    void track_WithMetadata() {
        // Given
        Map<String, Object> customMetadata = Map.of("customKey", "customValue");
        TrackingEventCommand event = new TrackingEventCommand(
                "event-1", "IMPRESSION", 1L, "session-123", "device-456",
                "2024-01-01T10:00:00Z", "feed", "REVIEW", "100",
                0, 1, 0.95, "request-abc", "hybrid-v1", 5000L, 75.5, 3000L, customMetadata
        );
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When
        service.track(command);

        // Then
        ArgumentCaptor<EventPayload> payloadCaptor = ArgumentCaptor.forClass(EventPayload.class);
        verify(eventPublisher).publish(eq(EventTopics.TRACKING), eq("1"), payloadCaptor.capture());

        EventPayload payload = payloadCaptor.getValue();
        Map<String, Object> metadata = payload.metadata();
        assertThat(metadata.get("eventId")).isEqualTo("event-1");
        assertThat(metadata.get("contentType")).isEqualTo("REVIEW");
        assertThat(metadata.get("contentId")).isEqualTo("100");
        assertThat(metadata.get("sessionId")).isEqualTo("session-123");
        assertThat(metadata.get("deviceId")).isEqualTo("device-456");
        assertThat(metadata.get("source")).isEqualTo("feed");
        assertThat(metadata.get("position")).isEqualTo(0);
        assertThat(metadata.get("rank")).isEqualTo(1);
        assertThat(metadata.get("score")).isEqualTo(0.95);
        assertThat(metadata.get("requestId")).isEqualTo("request-abc");
        assertThat(metadata.get("algorithm")).isEqualTo("hybrid-v1");
        assertThat(metadata.get("dwellMs")).isEqualTo(5000L);
        assertThat(metadata.get("scrollDepthPct")).isEqualTo(75.5);
        assertThat(metadata.get("visibilityMs")).isEqualTo(3000L);
        assertThat(metadata.get("customKey")).isEqualTo("customValue");
    }

    @Test
    @DisplayName("유효한 UUID eventId 파싱")
    void track_ValidUuidEventId() {
        // Given
        String validUuid = UUID.randomUUID().toString();
        TrackingEventCommand event = createEventWithParams(validUuid, "IMPRESSION", "REVIEW", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When
        service.track(command);

        // Then
        ArgumentCaptor<EventPayload> payloadCaptor = ArgumentCaptor.forClass(EventPayload.class);
        verify(eventPublisher).publish(eq(EventTopics.TRACKING), any(), payloadCaptor.capture());

        EventPayload payload = payloadCaptor.getValue();
        assertThat(payload.eventId()).isEqualTo(UUID.fromString(validUuid));
    }

    @Test
    @DisplayName("유효하지 않은 UUID eventId는 새 UUID 생성")
    void track_InvalidUuidEventId_GeneratesNewUuid() {
        // Given
        TrackingEventCommand event = createEventWithParams("invalid-uuid", "IMPRESSION", "REVIEW", "100", 1L);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

        // When
        service.track(command);

        // Then
        ArgumentCaptor<EventPayload> payloadCaptor = ArgumentCaptor.forClass(EventPayload.class);
        verify(eventPublisher).publish(eq(EventTopics.TRACKING), any(), payloadCaptor.capture());

        EventPayload payload = payloadCaptor.getValue();
        assertThat(payload.eventId()).isNotNull();
    }

    @Test
    @DisplayName("모든 TrackingEventType 값 처리")
    void track_AllEventTypes() {
        // Given
        String[] eventTypes = {"IMPRESSION", "CLICK", "DWELL", "SCROLL", "BOOKMARK", "LIKE", "FOLLOW", "REVIEW_CREATE", "REVIEW_UPDATE"};

        for (String eventType : eventTypes) {
            TrackingEventCommand event = createEventWithParams("event-" + eventType, eventType, "REVIEW", "100", 1L);
            TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

            // When
            service.track(command);

            // Then
            ArgumentCaptor<EventPayload> payloadCaptor = ArgumentCaptor.forClass(EventPayload.class);
            verify(eventPublisher, atLeastOnce()).publish(eq(EventTopics.TRACKING), any(), payloadCaptor.capture());
        }
    }

    @Test
    @DisplayName("모든 TrackingContentType 값 처리")
    void track_AllContentTypes() {
        // Given
        String[] contentTypes = {"REVIEW", "BOOK", "USER"};

        for (String contentType : contentTypes) {
            reset(eventPublisher);
            TrackingEventCommand event = createEventWithParams("event-" + contentType, "IMPRESSION", contentType, "100", 1L);
            TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(List.of(event));

            // When
            service.track(command);

            // Then
            ArgumentCaptor<EventPayload> payloadCaptor = ArgumentCaptor.forClass(EventPayload.class);
            verify(eventPublisher).publish(eq(EventTopics.TRACKING), any(), payloadCaptor.capture());

            EventPayload payload = payloadCaptor.getValue();
            assertThat(payload.targetType()).isEqualTo(contentType);
        }
    }

    private TrackingEventCommand createValidEvent() {
        return new TrackingEventCommand(
                "event-1", "IMPRESSION", 1L, "session-123", null, null,
                null, "REVIEW", "100", null, null, null, null, null, null, null, null, null
        );
    }

    private TrackingEventCommand createEventWithParams(String eventId, String eventType, String contentType, String contentId, Long userId) {
        return new TrackingEventCommand(
                eventId, eventType, userId, "session-123", null, null,
                null, contentType, contentId, null, null, null, null, null, null, null, null, null
        );
    }
}
