package org.yyubin.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.yyubin.batch.runner.BatchJobRunner;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final BatchJobRunner batchJobRunner;

    @Scheduled(cron = "${batch.schedule.neo4j:0 */10 * * * *}")
    @SchedulerLock(name = "neo4jSync", lockAtLeastFor = "5m", lockAtMostFor = "10m")
    public void syncNeo4j() {
        batchJobRunner.run("neo4jSyncJob");
    }

    @Scheduled(cron = "${batch.schedule.elasticsearch:0 */30 * * * *}")
    @SchedulerLock(name = "elasticsearchSync", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    public void syncElasticsearch() {
        batchJobRunner.run("elasticsearchSyncJob");
    }

    @Scheduled(cron = "${batch.schedule.view-flush:0 */15 * * * *}")
    @SchedulerLock(name = "reviewViewFlush", lockAtLeastFor = "1m", lockAtMostFor = "15m")
    public void flushReviewViews() {
        batchJobRunner.run("reviewViewFlushJob");
    }

    @Scheduled(cron = "${batch.schedule.recommendation:0 0 * * * *}")
    @SchedulerLock(name = "reviewContentSync", lockAtLeastFor = "5m", lockAtMostFor = "30m")
    public void syncReviewContent() {
        batchJobRunner.run("reviewContentSyncJob");
    }

    @Scheduled(cron = "${batch.schedule.search-query-log:0 */1 * * * *}")
    @SchedulerLock(name = "searchQueryLogFlush", lockAtLeastFor = "30s", lockAtMostFor = "2m")
    public void flushSearchQueryLogs() {
        batchJobRunner.run("searchQueryLogFlushJob");
    }
}
