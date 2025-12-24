package org.yyubin.application.tracking.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.tracking.TrackRecommendationEventsUseCase;
import org.yyubin.application.tracking.TrackingContentType;
import org.yyubin.application.tracking.TrackingEventType;
import org.yyubin.application.tracking.command.TrackRecommendationEventsCommand;
import org.yyubin.application.tracking.command.TrackingEventCommand;

@Service
@RequiredArgsConstructor
public class RecommendationTrackingService implements TrackRecommendationEventsUseCase {

    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void track(TrackRecommendationEventsCommand command) {
        List<TrackingEventCommand> events = command.events();
        for (TrackingEventCommand event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(TrackingEventCommand event) {
        if (event.eventId() == null || event.eventId().isBlank()) {
            throw new IllegalArgumentException("eventId is required");
        }
        if (event.eventType() == null || event.eventType().isBlank()) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (event.contentType() == null || event.contentType().isBlank()) {
            throw new IllegalArgumentException("contentType is required");
        }
        if (event.contentId() == null || event.contentId().isBlank()) {
            throw new IllegalArgumentException("contentId is required");
        }

        TrackingEventType eventType = parseEventType(event.eventType());
        TrackingContentType contentType = parseContentType(event.contentType());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventId", event.eventId());
        metadata.put("contentType", contentType.name());
        metadata.put("contentId", event.contentId());
        putIfNotNull(metadata, "sessionId", event.sessionId());
        putIfNotNull(metadata, "deviceId", event.deviceId());
        putIfNotNull(metadata, "clientTime", event.clientTime());
        putIfNotNull(metadata, "source", event.source());
        putIfNotNull(metadata, "position", event.position());
        putIfNotNull(metadata, "rank", event.rank());
        putIfNotNull(metadata, "score", event.score());
        putIfNotNull(metadata, "requestId", event.requestId());
        putIfNotNull(metadata, "algorithm", event.algorithm());
        putIfNotNull(metadata, "dwellMs", event.dwellMs());
        putIfNotNull(metadata, "scrollDepthPct", event.scrollDepthPct());
        putIfNotNull(metadata, "visibilityMs", event.visibilityMs());
        if (event.metadata() != null) {
            metadata.putAll(event.metadata());
        }

        String key = event.userId() != null ? event.userId().toString()
                : event.sessionId() != null ? event.sessionId() : "anonymous";

        EventPayload payload = new EventPayload(
                parseEventId(event.eventId()),
                eventType.name(),
                event.userId(),
                contentType.name(),
                event.contentId(),
                metadata,
                Instant.now(),
                "tracking-api",
                1
        );

        eventPublisher.publish(EventTopics.TRACKING, key, payload);
    }

    private TrackingEventType parseEventType(String value) {
        try {
            return TrackingEventType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported eventType: " + value);
        }
    }

    private TrackingContentType parseContentType(String value) {
        try {
            return TrackingContentType.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported contentType: " + value);
        }
    }

    private UUID parseEventId(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return UUID.randomUUID();
        }
    }

    private void putIfNotNull(Map<String, Object> metadata, String key, Object value) {
        if (value != null) {
            metadata.put(key, value);
        }
    }
}
