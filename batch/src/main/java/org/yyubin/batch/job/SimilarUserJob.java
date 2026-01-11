package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.usecase.FindSimilarUsersUseCase;
import org.yyubin.domain.recommendation.UserTasteVector;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 유사 사용자 계산 배치 작업
 * 모든 사용자에 대해 취향 벡터 기반 유사 사용자를 계산합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarUserJob {

    private final UserJpaRepository userRepository;
    private final ReviewCircleCachePort cachePort;
    private final FindSimilarUsersUseCase findSimilarUsersUseCase;

    /**
     * 매일 새벽 3시에 실행 (UserTasteProfileJob 이후)
     */
    @Scheduled(cron = "${batch.schedule.similarUser:0 0 3 * * *}")
    @SchedulerLock(
        name = "similarUser",
        lockAtLeastFor = "5m",
        lockAtMostFor = "3h"
    )
    public void findSimilarUsers() {
        log.info("유사 사용자 계산 배치 시작");

        try {
            // 1. 모든 사용자 ID 조회
            List<Long> userIds = userRepository.findAll().stream()
                .map(user -> user.getId())
                .toList();

            log.info("Processing {} users", userIds.size());

            // 2. 모든 사용자의 취향 벡터 로드
            log.info("Loading taste vectors from Redis...");
            List<UserTasteVector> allTasteVectors = new ArrayList<>();

            for (Long userId : userIds) {
                Optional<UserTasteVector> vectorOpt = cachePort.getTasteVector(userId);
                vectorOpt.ifPresent(allTasteVectors::add);
            }

            log.info("Loaded {} taste vectors (from {} users)",
                allTasteVectors.size(), userIds.size());

            if (allTasteVectors.isEmpty()) {
                log.warn("No taste vectors found - skipping similar user calculation");
                return;
            }

            // 3. 각 사용자의 유사 사용자 계산
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            for (UserTasteVector userVector : allTasteVectors) {
                try {
                    findSimilarUsersUseCase.execute(userVector.userId(), allTasteVectors);
                    successCount.incrementAndGet();

                    if (successCount.get() % 100 == 0) {
                        log.info("Progress: {}/{} users processed",
                            successCount.get(), allTasteVectors.size());
                    }

                } catch (Exception e) {
                    log.error("Failed to find similar users for user {}", userVector.userId(), e);
                    failureCount.incrementAndGet();
                }
            }

            log.info("유사 사용자 계산 배치 완료 - Success: {}, Failure: {}",
                successCount.get(), failureCount.get());

        } catch (Exception e) {
            log.error("유사 사용자 계산 배치 중 오류 발생", e);
            throw e;
        }
    }
}
