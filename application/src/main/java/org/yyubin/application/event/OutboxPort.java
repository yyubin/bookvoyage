package org.yyubin.application.event;

import java.util.List;

public interface OutboxPort {
    void save(String topic, String key, EventPayload payload);

    List<OutboxEvent> findPending(int limit);

    void markSent(Long id);

    void markFailed(Long id, String errorMessage);
}
