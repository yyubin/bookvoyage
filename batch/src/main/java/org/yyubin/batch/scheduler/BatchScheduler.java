package org.yyubin.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void syncNeo4j() {
        batchJobRunner.run("neo4jSyncJob");
    }

    @Scheduled(cron = "${batch.schedule.elasticsearch:0 */30 * * * *}")
    public void syncElasticsearch() {
        batchJobRunner.run("elasticsearchSyncJob");
    }

    @Scheduled(cron = "${batch.schedule.view-flush:0 */15 * * * *}")
    public void flushReviewViews() {
        batchJobRunner.run("reviewViewFlushJob");
    }
}
