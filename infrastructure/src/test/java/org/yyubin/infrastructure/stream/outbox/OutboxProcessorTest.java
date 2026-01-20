package org.yyubin.infrastructure.stream.outbox;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.OutboxEvent;
import org.yyubin.application.event.OutboxPort;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxProcessor 테스트")
class OutboxProcessorTest {

    @Mock
    private OutboxPort outboxPort;

    @Mock
    private KafkaTemplate<String, EventPayload> kafkaTemplate;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private OutboxProcessor processor;

    @Test
    @DisplayName("락 획득 실패 시 처리하지 않는다")
    void process_LockNotAcquired_NoOp() throws Exception {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), any())).thenReturn(false);
        when(lock.isHeldByCurrentThread()).thenReturn(false);

        // When
        processor.process();

        // Then
        verify(outboxPort, never()).findPending(anyInt());
        verify(lock, never()).unlock();
    }

    @Test
    @DisplayName("최대 재시도 초과 이벤트는 DEAD로 마킹한다")
    void process_MaxRetry_MarksDead() throws Exception {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), any())).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        ReflectionTestUtils.setField(processor, "batchSize", 10);

        EventPayload payload = new EventPayload(null, "EVENT", 1L, null, null, null, null, null, 1);
        OutboxEvent event = new OutboxEvent(1L, "topic", "key", payload, Instant.now(), OutboxEvent.OutboxStatus.PENDING, 5, null);
        when(outboxPort.findPending(10)).thenReturn(List.of(event));

        // When
        processor.process();

        // Then
        verify(outboxPort).markDead(1L, "Max retry count exceeded: 5");
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(EventPayload.class));
        verify(lock).unlock();
    }

    @Test
    @DisplayName("발행 성공 시 SENT로 마킹한다")
    void process_PublishSuccess_MarksSent() throws Exception {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), any())).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        ReflectionTestUtils.setField(processor, "batchSize", 10);

        EventPayload payload = new EventPayload(null, "EVENT", 1L, null, null, null, null, null, 1);
        OutboxEvent event = new OutboxEvent(2L, "topic", "key", payload, Instant.now(), OutboxEvent.OutboxStatus.PENDING, 0, null);
        when(outboxPort.findPending(10)).thenReturn(List.of(event));
        when(kafkaTemplate.send("topic", "key", payload)).thenReturn(CompletableFuture.completedFuture(null));

        // When
        processor.process();

        // Then
        verify(outboxPort).markSent(2L);
        verify(lock).unlock();
    }

    @Test
    @DisplayName("발행 실패 시 FAILED로 마킹한다")
    void process_PublishFailure_MarksFailed() throws Exception {
        // Given
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong(), any())).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        ReflectionTestUtils.setField(processor, "batchSize", 10);

        EventPayload payload = new EventPayload(null, "EVENT", 1L, null, null, null, null, null, 1);
        OutboxEvent event = new OutboxEvent(3L, "topic", "key", payload, Instant.now(), OutboxEvent.OutboxStatus.PENDING, 0, null);
        when(outboxPort.findPending(10)).thenReturn(List.of(event));
        when(kafkaTemplate.send("topic", "key", payload))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("fail")));

        // When
        processor.process();

        // Then
        verify(outboxPort).markFailed(eq(3L), contains("fail"));
        verify(lock).unlock();
    }
}
