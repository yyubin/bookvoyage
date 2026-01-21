package org.yyubin.batch.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SyncTimestampListener 테스트")
class SyncTimestampListenerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private StepExecution stepExecution;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private JobInstance jobInstance;

    private SyncTimestampListener syncTimestampListener;

    @BeforeEach
    void setUp() {
        syncTimestampListener = new SyncTimestampListener(redisTemplate);
    }

    @Test
    @DisplayName("Step 완료 후 동기화 시각 저장")
    void afterStep_SavesSyncTime() {
        // Given
        String jobName = "testJob";
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn(jobName);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        ExitStatus result = syncTimestampListener.afterStep(stepExecution);

        // Then
        assertThat(result).isNull();
        verify(valueOperations).set(eq("batch:lastSync:" + jobName), anyString());
    }

    @Test
    @DisplayName("저장되는 키 형식 확인")
    void afterStep_CorrectKeyFormat() {
        // Given
        String jobName = "elasticsearchSyncJob";
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn(jobName);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        syncTimestampListener.afterStep(stepExecution);

        // Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(keyCaptor.capture(), anyString());
        assertThat(keyCaptor.getValue()).isEqualTo("batch:lastSync:elasticsearchSyncJob");
    }

    @Test
    @DisplayName("저장되는 시간 형식 확인")
    void afterStep_TimeFormat() {
        // Given
        String jobName = "testJob";
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(jobInstance.getJobName()).thenReturn(jobName);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // When
        syncTimestampListener.afterStep(stepExecution);

        // Then
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(anyString(), valueCaptor.capture());
        String savedTime = valueCaptor.getValue();
        assertThat(savedTime).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }

    @Test
    @DisplayName("다양한 Job 이름에 대해 동작 확인")
    void afterStep_VariousJobNames() {
        // Given
        String[] jobNames = {"neo4jSyncJob", "reviewViewFlushJob", "searchQueryLogFlushJob"};
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getJobInstance()).thenReturn(jobInstance);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        for (String jobName : jobNames) {
            // Given
            when(jobInstance.getJobName()).thenReturn(jobName);

            // When
            syncTimestampListener.afterStep(stepExecution);

            // Then
            verify(valueOperations).set(eq("batch:lastSync:" + jobName), anyString());
        }
    }
}
