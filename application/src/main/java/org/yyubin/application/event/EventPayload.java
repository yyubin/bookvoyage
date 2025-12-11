package org.yyubin.application.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record EventPayload(
        UUID eventId,
        String eventType,
        Long userId,
        String targetType,
        String targetId,
        Map<String, Object> metadata,
        Instant occurredAt,
        String source,
        int schemaVersion
) {
    public EventPayload {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (schemaVersion <= 0) {
            schemaVersion = 1;
        }
    }
}
