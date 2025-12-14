package org.yyubin.application.event;

import java.util.List;

public interface OutboxPort {
    void save(String topic, String key, EventPayload payload);

    List<OutboxEvent> findPending(int limit);

    void markSent(Long id);

    void markFailed(Long id, String errorMessage);

    /**
     * 최대 재시도 횟수 초과 시 DEAD 상태로 변경 (DLQ)
     */
    void markDead(Long id, String errorMessage);
}
