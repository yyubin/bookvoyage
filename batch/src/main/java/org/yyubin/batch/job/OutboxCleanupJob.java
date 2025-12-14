package org.yyubin.batch.job;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.OutboxEvent;
import org.yyubin.application.event.OutboxPort;

/**
 * Outbox 이벤트 정리 배치 작업
 * 7일 이전의 발행 완료(SENT) 이벤트를 삭제하여 DB 용량 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCleanupJob {

    private final OutboxPort outboxPort;

    /**
     * 매일 새벽 2시에 오래된 Outbox 이벤트 삭제
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @SchedulerLock(name = "outboxCleanup", lockAtLeastFor = "1m", lockAtMostFor = "10m")
    public void cleanupOldOutboxEvents() {
        log.info("Outbox 이벤트 정리 작업 시작");

        try {
            Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);
            int deletedCount = outboxPort.deleteByStatusAndOccurredAtBefore(
                    OutboxEvent.OutboxStatus.SENT,
                    cutoff
            );

            log.info("Outbox 이벤트 정리 완료: {} 건 삭제됨", deletedCount);
        } catch (Exception e) {
            log.error("Outbox 이벤트 정리 중 오류 발생", e);
            throw e;
        }
    }
}
