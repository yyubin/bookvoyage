package org.yyubin.infrastructure.persistence.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.OutboxEvent;
import org.yyubin.application.event.OutboxPort;

@Component
@RequiredArgsConstructor
public class OutboxPersistenceAdapter implements OutboxPort {

    private final OutboxEventJpaRepository outboxEventJpaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void save(String topic, String key, EventPayload payload) {
        outboxEventJpaRepository.save(
                OutboxEventEntity.builder()
                        .topic(topic)
                        .key(key)
                        .payload(writePayload(payload))
                        .occurredAt(payload.occurredAt())
                        .status(OutboxEvent.OutboxStatus.PENDING)
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutboxEvent> findPending(int limit) {
        return outboxEventJpaRepository.findByStatusOrderByOccurredAtAsc(
                        OutboxEvent.OutboxStatus.PENDING,
                        PageRequest.of(0, limit)
                )
                .stream()
                .map(entity -> entity.toDomain(this::readPayload))
                .toList();
    }

    @Override
    @Transactional
    public void markSent(Long id) {
        outboxEventJpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(OutboxEvent.OutboxStatus.SENT);
            entity.setUpdatedAt(Instant.now());
        });
    }

    @Override
    @Transactional
    public void markFailed(Long id, String errorMessage) {
        outboxEventJpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(OutboxEvent.OutboxStatus.PENDING); // 재시도를 위해 PENDING으로 변경
            entity.setRetryCount(entity.getRetryCount() + 1);
            entity.setLastError(errorMessage != null ? errorMessage.substring(0, Math.min(500, errorMessage.length())) : null);
            entity.setUpdatedAt(Instant.now());
        });
    }

    @Override
    @Transactional
    public void markDead(Long id, String errorMessage) {
        outboxEventJpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(OutboxEvent.OutboxStatus.DEAD);
            entity.setLastError(errorMessage != null ? errorMessage.substring(0, Math.min(500, errorMessage.length())) : null);
            entity.setUpdatedAt(Instant.now());
        });
    }

    private String writePayload(EventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize event payload", e);
        }
    }

    private EventPayload readPayload(String json) {
        try {
            return objectMapper.readValue(json, EventPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize event payload", e);
        }
    }
}
