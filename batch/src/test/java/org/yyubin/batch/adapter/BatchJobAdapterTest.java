package org.yyubin.batch.adapter;

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
import org.yyubin.application.batch.dto.BatchJobResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchJobAdapter 테스트")
class BatchJobAdapterTest {

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

    private BatchJobAdapter batchJobAdapter;

    @BeforeEach
    void setUp() {
        batchJobAdapter = new BatchJobAdapter(jobRegistry, jobRepository, jobExplorer);
    }

    @Test
    @DisplayName("Job 실행 성공")
    void runJob_Success() throws Exception {
        // Given
        String jobName = "testJob";
        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(null);
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);

        // When
        BatchJobResult result = batchJobAdapter.runJob(jobName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.jobName()).isEqualTo(jobName);
        assertThat(result.status()).isEqualTo("STARTED");
        assertThat(result.executionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Job이 존재하지 않을 때 에러 반환")
    void runJob_JobNotFound() throws Exception {
        // Given
        String jobName = "nonExistentJob";
        when(jobRegistry.getJob(jobName)).thenThrow(new RuntimeException("Job not found"));

        // When
        BatchJobResult result = batchJobAdapter.runJob(jobName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.jobName()).isEqualTo(jobName);
        assertThat(result.status()).isEqualTo("ERROR");
        assertThat(result.message()).contains("Job not found");
    }

    @Test
    @DisplayName("Job 실행 중인지 확인 - 실행 중")
    void isJobRunning_Running() {
        // Given
        String jobName = "testJob";
        JobExecution runningExecution = mock(JobExecution.class);
        BatchStatus runningStatus = mock(BatchStatus.class);
        when(runningStatus.isRunning()).thenReturn(true);
        when(runningExecution.getStatus()).thenReturn(runningStatus);
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Set.of(runningExecution));

        // When
        boolean result = batchJobAdapter.isJobRunning(jobName);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Job 실행 중인지 확인 - 실행 중 아님")
    void isJobRunning_NotRunning() {
        // Given
        String jobName = "testJob";
        when(jobRepository.findRunningJobExecutions(jobName)).thenReturn(Collections.emptySet());

        // When
        boolean result = batchJobAdapter.isJobRunning(jobName);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("마지막 실행 시간 조회 - 이전 실행 없음")
    void runJob_NoLastExecution() throws Exception {
        // Given
        String jobName = "testJob";
        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(null);
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);

        // When
        BatchJobResult result = batchJobAdapter.runJob(jobName);

        // Then
        assertThat(result.status()).isEqualTo("STARTED");
        verify(jobExplorer).getLastJobInstance(jobName);
    }

    @Test
    @DisplayName("마지막 실행 시간 조회 - 이전 실행 있음")
    void runJob_WithLastExecution() throws Exception {
        // Given
        String jobName = "testJob";
        JobExecution lastExecution = mock(JobExecution.class);
        when(lastExecution.getEndTime()).thenReturn(LocalDateTime.now().minusHours(1));

        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(jobInstance);
        when(jobExplorer.getJobExecutions(jobInstance)).thenReturn(List.of(lastExecution));
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class))).thenReturn(jobInstance);
        when(jobRepository.createJobExecution(eq(jobInstance), any(JobParameters.class), any(ExecutionContext.class)))
                .thenReturn(jobExecution);
        when(jobExecution.getId()).thenReturn(1L);

        // When
        BatchJobResult result = batchJobAdapter.runJob(jobName);

        // Then
        assertThat(result.status()).isEqualTo("STARTED");
        verify(jobExplorer).getJobExecutions(jobInstance);
    }

    @Test
    @DisplayName("JobRepository 예외 발생 시 에러 반환")
    void runJob_RepositoryException() throws Exception {
        // Given
        String jobName = "testJob";
        when(jobRegistry.getJob(jobName)).thenReturn(job);
        when(job.getName()).thenReturn(jobName);
        when(jobExplorer.getLastJobInstance(jobName)).thenReturn(null);
        when(jobRepository.createJobInstance(eq(jobName), any(JobParameters.class)))
                .thenThrow(new RuntimeException("Repository error"));

        // When
        BatchJobResult result = batchJobAdapter.runJob(jobName);

        // Then
        assertThat(result.status()).isEqualTo("ERROR");
        assertThat(result.message()).contains("Repository error");
    }
}
