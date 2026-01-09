package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.usecase.AnalyzeCommunityTrendUseCase;
import org.yyubin.domain.recommendation.CommunityTrend;

/**
 * 커뮤니티 트렌드 분석 배치 작업
 * 일정한 주기로 현재 독서 커뮤니티의 전반적인 경향성을 분석합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityTrendAnalysisJob {

    private final AnalyzeCommunityTrendUseCase useCase;

    /**
     * 매 1시간마다 커뮤니티 트렌드 분석 실행
     * 스케줄링 및 분산 환경에서의 중복 실행 방지를 위해 ShedLock 사용
     */
    @Scheduled(cron = "${batch.schedule.communityTrend:0 0 * * * *}")
    @SchedulerLock(
        name = "communityTrendAnalysis",
        lockAtLeastFor = "2m",
        lockAtMostFor = "10m"
    )
    public void analyzeCommunityTrend() {
        log.info("커뮤니티 트렌드 분석 배치 시작");

        try {
            CommunityTrend result = useCase.execute();
            log.info("커뮤니티 트렌드 분석 완료 - keywords: {}, genres: {}",
                result.keywords(),
                result.genres().size()
            );
        } catch (Exception e) {
            log.error("커뮤니티 트렌드 분석 중 오류 발생", e);
            throw e;
        }
    }
}
