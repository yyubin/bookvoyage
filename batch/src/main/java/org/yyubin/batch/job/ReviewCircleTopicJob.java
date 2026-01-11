package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.usecase.AggregateReviewCircleTopicsUseCase;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 리뷰 서클 토픽 집계 배치 작업
 * 유사 사용자들의 최근 리뷰 키워드를 집계하여 트렌딩 토픽을 계산합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewCircleTopicJob {

    private final UserJpaRepository userRepository;
    private final ReviewCircleCachePort cachePort;
    private final AggregateReviewCircleTopicsUseCase aggregateTopicsUseCase;

    private static final String[] WINDOWS = {"24h", "7d"};

    /**
     * 매시간 실행
     */
    @Scheduled(cron = "${batch.schedule.reviewCircleTopic:0 0 * * * *}")
    @SchedulerLock(
        name = "reviewCircleTopic",
        lockAtLeastFor = "2m",
        lockAtMostFor = "30m"
    )
    public void aggregateReviewCircleTopics() {
        log.info("리뷰 서클 토픽 집계 배치 시작");

        try {
            // 1. 모든 사용자 ID 조회
            List<Long> userIds = userRepository.findAll().stream()
                .map(user -> user.getId())
                .toList();

            log.info("Processing {} users for {} windows", userIds.size(), WINDOWS.length);

            AtomicInteger totalSuccess = new AtomicInteger(0);
            AtomicInteger totalFailure = new AtomicInteger(0);

            // 2. 각 윈도우에 대해 집계
            for (String window : WINDOWS) {
                log.info("Processing window: {}", window);

                AtomicInteger successCount = new AtomicInteger(0);
                AtomicInteger failureCount = new AtomicInteger(0);

                for (Long userId : userIds) {
                    try {
                        // 유사 사용자가 있는지 확인
                        var similarUsers = cachePort.getSimilarUsers(userId);
                        if (similarUsers.isEmpty()) {
                            continue;
                        }

                        aggregateTopicsUseCase.execute(userId, window);
                        successCount.incrementAndGet();

                        if (successCount.get() % 100 == 0) {
                            log.info("Progress [{}]: {}/{} users processed",
                                window, successCount.get(), userIds.size());
                        }

                    } catch (Exception e) {
                        log.error("Failed to aggregate topics for user {} window {}", userId, window, e);
                        failureCount.incrementAndGet();
                    }
                }

                totalSuccess.addAndGet(successCount.get());
                totalFailure.addAndGet(failureCount.get());

                log.info("Window {} completed - Success: {}, Failure: {}",
                    window, successCount.get(), failureCount.get());
            }

            log.info("리뷰 서클 토픽 집계 배치 완료 - Total Success: {}, Total Failure: {}",
                totalSuccess.get(), totalFailure.get());

        } catch (Exception e) {
            log.error("리뷰 서클 토픽 집계 배치 중 오류 발생", e);
            throw e;
        }
    }
}
