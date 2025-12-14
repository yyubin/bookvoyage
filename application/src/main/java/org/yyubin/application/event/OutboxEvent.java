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
        PENDING,    // 발행 대기 중
        SENT,       // 발행 완료
        FAILED,     // 발행 실패 (재시도 가능)
        DEAD        // 최대 재시도 초과 (DLQ)
    }
}
