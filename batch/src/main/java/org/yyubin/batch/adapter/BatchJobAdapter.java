package org.yyubin.batch.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;
import org.yyubin.application.batch.dto.BatchJobResult;
import org.yyubin.application.batch.port.BatchJobPort;

import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobAdapter implements BatchJobPort {

    private final JobRegistry jobRegistry;
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;

    @Override
    public BatchJobResult runJob(String jobName) {
        try {
            Job job = jobRegistry.getJob(jobName);

            long lastRunAtEpochMs = resolveLastRunAtEpochMs(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("lastRunAtEpochMs", lastRunAtEpochMs)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobInstance jobInstance = jobRepository.createJobInstance(job.getName(), jobParameters);
            ExecutionContext executionContext = new ExecutionContext();
            JobExecution jobExecution = jobRepository.createJobExecution(
                    jobInstance,
                    jobParameters,
                    executionContext
            );

            log.info("Starting batch job {} (executionId={})", jobName, jobExecution.getId());

            // 비동기로 실행
            new Thread(() -> {
                try {
                    job.execute(jobExecution);
                    log.info("Batch job {} finished with status {}", jobName, jobExecution.getStatus());
                } catch (Exception e) {
                    log.error("Batch job {} failed", jobName, e);
                }
            }).start();

            return BatchJobResult.success(jobName, jobExecution.getId());

        } catch (Exception e) {
            log.error("Failed to start batch job {}", jobName, e);
            return BatchJobResult.error(jobName, e.getMessage());
        }
    }

    @Override
    public boolean isJobRunning(String jobName) {
        return jobRepository.findRunningJobExecutions(jobName)
                .stream()
                .anyMatch(e -> e.getStatus().isRunning());
    }

    private long resolveLastRunAtEpochMs(String jobName) {
        JobInstance lastInstance = jobExplorer.getLastJobInstance(jobName);
        if (lastInstance == null) {
            return 0L;
        }
        return jobExplorer.getJobExecutions(lastInstance).stream()
                .map(JobExecution::getEndTime)
                .filter(Objects::nonNull)
                .map(date -> date.toInstant(ZoneOffset.UTC).toEpochMilli())
                .max(Comparator.naturalOrder())
                .orElse(0L);
    }
}
