package org.yyubin.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobRunner {

    private final JobRegistry jobRegistry;
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;

    public void run(String jobName) {
        try {
            Job job = jobRegistry.getJob(jobName);

            if (isRunning(jobName)) {
                log.warn("Job {} is already running. Skip.", jobName);
                return;
            }

            long lastRunAtEpochMs = resolveLastRunAtEpochMs(jobName);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("lastRunAtEpochMs", lastRunAtEpochMs)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobInstance jobInstance =
                    jobRepository.createJobInstance(job.getName(), jobParameters);

            ExecutionContext executionContext = new ExecutionContext();

            JobExecution jobExecution =
                    jobRepository.createJobExecution(
                            jobInstance,
                            jobParameters,
                            executionContext
                    );

            log.info("Starting job {} (executionId={})",
                    jobName, jobExecution.getId());

            job.execute(jobExecution);

            log.info("Job {} finished with status {}",
                    jobName, jobExecution.getStatus());

        } catch (Exception e) {
            log.error("Job {} failed", jobName, e);
        }
    }

    private boolean isRunning(String jobName) {
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
                .filter(java.util.Objects::nonNull)
                .map(date -> date.toInstant(java.time.ZoneOffset.UTC).toEpochMilli())
                .max(java.util.Comparator.naturalOrder())
                .orElse(0L);
    }
}
