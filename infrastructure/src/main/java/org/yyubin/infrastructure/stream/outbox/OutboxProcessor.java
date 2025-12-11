package org.yyubin.infrastructure.stream.outbox;

import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.OutboxEvent;
import org.yyubin.application.event.OutboxPort;
import org.yyubin.infrastructure.stream.kafka.KafkaEventPublisher;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxPort outboxPort;
    private final KafkaTemplate<String, org.yyubin.application.event.EventPayload> kafkaTemplate;

    @Value("${outbox.processor.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${outbox.processor.fixed-delay-ms:1000}", initialDelayString = "${outbox.processor.initial-delay-ms:1000}")
    public void process() {
        List<OutboxEvent> events = outboxPort.findPending(batchSize);
        if (events.isEmpty()) {
            return;
        }
        for (OutboxEvent event : events) {
            try {
                kafkaTemplate.send(event.topic(), event.key(), event.payload()).get();
                outboxPort.markSent(event.id());
            } catch (Exception ex) {
                log.warn("Failed to publish outbox event id={} topic={} error={}", event.id(), event.topic(), ex.toString());
                outboxPort.markFailed(event.id(), ex.getMessage());
            }
        }
    }
}
