package org.yyubin.infrastructure.stream.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.OutboxPort;

@Component
@Primary
@RequiredArgsConstructor
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxPort outboxPort;

    @Override
    public void publish(String topic, String key, EventPayload payload) {
        outboxPort.save(topic, key, payload);
    }
}
