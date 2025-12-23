package org.yyubin.batch.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.OutboxEvent;
import org.yyubin.application.event.OutboxPort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxCleanupJob 테스트")
class OutboxCleanupJobTest {

    @Mock
    private OutboxPort outboxPort;

    @InjectMocks
    private OutboxCleanupJob outboxCleanupJob;

    @Test
    @DisplayName("오래된 Outbox 이벤트 삭제 성공")
    void cleanupOldOutboxEvents_Success() {
        // Given
        int deletedCount = 42;
        when(outboxPort.deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        )).thenReturn(deletedCount);

        // When
        outboxCleanupJob.cleanupOldOutboxEvents();

        // Then
        ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(outboxPort).deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                cutoffCaptor.capture()
        );

        Instant capturedCutoff = cutoffCaptor.getValue();
        Instant expectedCutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        // 시간 차이가 1초 이내인지 확인
        assertThat(capturedCutoff).isCloseTo(expectedCutoff, within(1, ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("삭제할 이벤트가 없는 경우")
    void cleanupOldOutboxEvents_NoEventsToDelete() {
        // Given
        when(outboxPort.deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        )).thenReturn(0);

        // When
        outboxCleanupJob.cleanupOldOutboxEvents();

        // Then
        verify(outboxPort).deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        );
    }

    @Test
    @DisplayName("대량의 이벤트 삭제 성공")
    void cleanupOldOutboxEvents_LargeNumberOfEvents() {
        // Given
        int deletedCount = 10000;
        when(outboxPort.deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        )).thenReturn(deletedCount);

        // When
        outboxCleanupJob.cleanupOldOutboxEvents();

        // Then
        verify(outboxPort).deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        );
    }

    @Test
    @DisplayName("정리 작업 중 예외 발생 시 예외를 던짐")
    void cleanupOldOutboxEvents_ThrowsException() {
        // Given
        when(outboxPort.deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        )).thenThrow(new RuntimeException("Database error"));

        // When & Then
        try {
            outboxCleanupJob.cleanupOldOutboxEvents();
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Database error");
        }

        verify(outboxPort).deleteByStatusAndOccurredAtBefore(
                eq(OutboxEvent.OutboxStatus.SENT),
                any(Instant.class)
        );
    }
}
