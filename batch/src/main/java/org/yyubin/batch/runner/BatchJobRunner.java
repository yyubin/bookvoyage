package org.yyubin.batch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.JobRegistry;
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

    public void run(String jobName) {
        try {
            Job job = jobRegistry.getJob(jobName);

            if (isRunning(jobName)) {
                log.warn("Job {} is already running. Skip.", jobName);
                return;
            }

            JobParameters jobParameters = new JobParametersBuilder()
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
}
