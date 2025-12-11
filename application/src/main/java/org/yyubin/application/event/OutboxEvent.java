package org.yyubin.application.event;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        Long id,
        String topic,
        String key,
        EventPayload payload,
        Instant occurredAt,
        OutboxStatus status,
        int retryCount,
        String lastError
) {
    public enum OutboxStatus {
        PENDING, SENT, FAILED
    }
}
