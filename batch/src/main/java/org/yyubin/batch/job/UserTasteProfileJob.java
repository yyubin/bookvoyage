package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.usecase.BuildUserTasteVectorUseCase;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 사용자 취향 프로필 배치 작업
 * 모든 사용자의 취향 벡터를 계산하여 Redis에 캐싱합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTasteProfileJob {

    private final UserJpaRepository userRepository;
    private final BuildUserTasteVectorUseCase buildUserTasteVectorUseCase;

    /**
     * 매일 새벽 2시에 실행
     */
    @Scheduled(cron = "${batch.schedule.userTasteProfile:0 0 2 * * *}")
    @SchedulerLock(
        name = "userTasteProfile",
        lockAtLeastFor = "5m",
        lockAtMostFor = "2h"
    )
    public void buildUserTasteVectors() {
        log.info("사용자 취향 프로필 배치 시작");

        try {
            // 모든 사용자 ID 조회
            List<Long> userIds = userRepository.findAll().stream()
                .map(user -> user.getId())
                .toList();

            log.info("Processing {} users", userIds.size());

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // 각 사용자의 취향 벡터 생성
            for (Long userId : userIds) {
                try {
                    buildUserTasteVectorUseCase.execute(userId);
                    successCount.incrementAndGet();

                    if (successCount.get() % 100 == 0) {
                        log.info("Progress: {}/{} users processed", successCount.get(), userIds.size());
                    }

                } catch (Exception e) {
                    log.error("Failed to build taste vector for user {}", userId, e);
                    failureCount.incrementAndGet();
                }
            }

            log.info("사용자 취향 프로필 배치 완료 - Success: {}, Failure: {}",
                successCount.get(), failureCount.get());

        } catch (Exception e) {
            log.error("사용자 취향 프로필 배치 중 오류 발생", e);
            throw e;
        }
    }
}
