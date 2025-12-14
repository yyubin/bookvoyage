package org.yyubin.infrastructure.stream.outbox;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.OutboxEvent;
import org.yyubin.application.event.OutboxPort;

/**
 * Outbox 이벤트를 Kafka로 발행하는 프로세서
 * - 분산 락을 사용하여 여러 인스턴스에서 중복 처리 방지
 * - 재시도 횟수 제한으로 무한 재시도 방지
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private static final String LOCK_KEY = "outbox:processor:lock";
    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxPort outboxPort;
    private final KafkaTemplate<String, org.yyubin.application.event.EventPayload> kafkaTemplate;
    private final RedissonClient redissonClient;

    @Value("${outbox.processor.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${outbox.processor.fixed-delay-ms:1000}", initialDelayString = "${outbox.processor.initial-delay-ms:1000}")
    public void process() {
        RLock lock = redissonClient.getLock(LOCK_KEY);

        try {
            // 분산 락 획득 시도 (최대 100ms 대기, 락 유지 시간 10초)
            boolean acquired = lock.tryLock(100, 10000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.debug("Could not acquire outbox processor lock, skipping this round");
                return;
            }

            processEvents();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Outbox processor interrupted", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void processEvents() {
        List<OutboxEvent> events = outboxPort.findPending(batchSize);
        if (events.isEmpty()) {
            return;
        }

        log.debug("Processing {} outbox events", events.size());

        for (OutboxEvent event : events) {
            // 최대 재시도 횟수 체크
            if (event.retryCount() >= MAX_RETRY_COUNT) {
                log.error("Outbox event id={} exceeded max retry count ({}), moving to DEAD",
                    event.id(), MAX_RETRY_COUNT);
                outboxPort.markDead(event.id(), "Max retry count exceeded: " + MAX_RETRY_COUNT);
                continue;
            }

            try {
                // Kafka로 이벤트 발행
                kafkaTemplate.send(event.topic(), event.key(), event.payload()).get();
                outboxPort.markSent(event.id());
                log.debug("Successfully published outbox event id={} topic={}", event.id(), event.topic());
            } catch (Exception ex) {
                log.warn("Failed to publish outbox event id={} topic={} retryCount={} error={}",
                    event.id(), event.topic(), event.retryCount(), ex.toString());
                outboxPort.markFailed(event.id(), ex.getMessage());
            }
        }
    }
}
