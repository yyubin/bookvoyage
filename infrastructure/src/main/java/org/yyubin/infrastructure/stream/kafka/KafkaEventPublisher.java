package org.yyubin.infrastructure.stream.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, EventPayload> kafkaTemplate;

    @Override
    public void publish(String topic, String key, EventPayload payload) {
        kafkaTemplate.send(topic, key, payload);
    }
}
