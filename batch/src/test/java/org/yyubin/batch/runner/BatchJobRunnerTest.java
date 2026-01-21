package org.yyubin.batch.runner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.explore.JobExplorer;
import org.springframework.batch.infrastructure.item.ExecutionContext;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchJobRunner 테스트")
class BatchJobRunnerTest {

    @Mock
    private JobRegistry jobRegistry;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobExplorer jobExplorer;

    @Mock
    private Job job;

    @Mock
    private JobInstance jobInstance;

    @Mock
    private JobExecution jobExecution;

    private BatchJobRunner batchJobRunner;

    @BeforeEach
    void setUp() {
        batchJobRunner = new BatchJobRunner(jobRegistry, jobRepository, jobExplorer);
    }

    @Test
    @DisplayName("Job 실행 성공")
    void run_Success() throws Exception {
        // Given
        String jobName = "testJob";
        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Collections.emptySet());
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(null);
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(job).execute(jobExecution);
    }

    @Test
    @DisplayName("Job이 이미 실행 중일 때 스킵")
    void run_SkipWhenAlreadyRunning() throws Exception {
        // Given
        String jobName = "testJob";
        JobExecution runningExecution = mock(JobExecution.class);
        BatchStatus runningStatus = mock(BatchStatus.class);
        when(runningStatus.isRunning()).thenReturn(true);
        when(runningExecution.getStatus()).thenReturn(runningStatus);

        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Set.of(runningExecution));

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(job, never()).execute(any());
    }

    @Test
    @DisplayName("Job이 존재하지 않을 때 예외 처리")
    void run_JobNotFound() throws Exception {
        // Given
        String jobName = "nonExistentJob";
        when(jobRegistry.getJob(jobName)).thenThrow(new RuntimeException("Job not found"));

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(jobRepository, never()).createJobInstance(anyString(), any());
    }

    @Test
    @DisplayName("마지막 실행 시간 계산 - 이전 실행 있음")
    void run_WithLastExecution() throws Exception {
        // Given
        String jobName = "testJob";
        JobExecution lastExecution = mock(JobExecution.class);
        when(lastExecution.getEndTime()).thenReturn(LocalDateTime.now().minusHours(1));

        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Collections.emptySet());
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(jobInstance);
        when(jobExplorer.getJobExecutions(jobInstance)).thenReturn(List.of(lastExecution));
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(job).execute(jobExecution);
        verify(jobExplorer).getJobExecutions(jobInstance);
    }

    @Test
    @DisplayName("마지막 실행 시간 계산 - endTime이 null인 경우")
    void run_LastExecutionWithNullEndTime() throws Exception {
        // Given
        String jobName = "testJob";
        JobExecution lastExecution = mock(JobExecution.class);
        when(lastExecution.getEndTime()).thenReturn(null);

        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Collections.emptySet());
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(jobInstance);
        when(jobExplorer.getJobExecutions(jobInstance)).thenReturn(List.of(lastExecution));
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(job).execute(jobExecution);
    }

    @Test
    @DisplayName("Job 실행 중 예외 발생")
    void run_ExecutionException() throws Exception {
        // Given
        String jobName = "testJob";
        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Collections.emptySet());
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(null);
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);
        doThrow(new RuntimeException("Execution failed")).when(job).execute(jobExecution);

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(job).execute(jobExecution);
    }

    @Test
    @DisplayName("실행 중인 Job이 여러 개일 때")
    void run_MultipleRunningExecutions() throws Exception {
        // Given
        String jobName = "testJob";
        JobExecution runningExecution1 = mock(JobExecution.class);
        JobExecution runningExecution2 = mock(JobExecution.class);
        BatchStatus runningStatus = mock(BatchStatus.class);
        BatchStatus completedStatus = mock(BatchStatus.class);

        // Use lenient since Set iteration order is not guaranteed and anyMatch may short-circuit
        lenient().when(runningStatus.isRunning()).thenReturn(true);
        lenient().when(completedStatus.isRunning()).thenReturn(false);
        lenient().when(runningExecution1.getStatus()).thenReturn(completedStatus);
        lenient().when(runningExecution2.getStatus()).thenReturn(runningStatus);

        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Set.of(runningExecution1, runningExecution2));

        // When
        batchJobRunner.run(jobName);

        // Then
        verify(job, never()).execute(any());
    }
}
