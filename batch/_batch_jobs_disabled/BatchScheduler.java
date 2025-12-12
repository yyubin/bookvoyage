package org.yyubin.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 배치 Job 스케줄러
 * - 주기적으로 동기화 Job 실행
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job neo4jSyncJob;
    private final Job elasticsearchSyncJob;

    /**
     * Neo4j 동기화 (10분마다)
     */
    @Scheduled(cron = "${batch.schedule.neo4j:0 */10 * * * *}")
    public void syncToNeo4j() {
        try {
            log.info("Starting Neo4j sync job");
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(neo4jSyncJob, params);
            log.info("Neo4j sync job completed");
        } catch (Exception e) {
            log.error("Neo4j sync job failed", e);
        }
    }

    /**
     * Elasticsearch 동기화 (30분마다)
     */
    @Scheduled(cron = "${batch.schedule.elasticsearch:0 */30 * * * *}")
    public void syncToElasticsearch() {
        try {
            log.info("Starting Elasticsearch sync job");
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(elasticsearchSyncJob, params);
            log.info("Elasticsearch sync job completed");
        } catch (Exception e) {
            log.error("Elasticsearch sync job failed", e);
        }
    }
}
