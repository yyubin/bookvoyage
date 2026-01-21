package org.yyubin.batch.job;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.yyubin.batch.service.SearchQueryLogStreamFlusher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchQueryLogFlushJobConfig 테스트")
class SearchQueryLogFlushJobConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private SearchQueryLogStreamFlusher streamFlusher;

    private SearchQueryLogFlushJobConfig config;

    @BeforeEach
    void setUp() {
        config = new SearchQueryLogFlushJobConfig(jobRepository, streamFlusher);
    }

    @Test
    @DisplayName("searchQueryLogFlushJob Bean 생성 성공")
    void searchQueryLogFlushJob_Created() {
        // Given
        Step step = config.searchQueryLogFlushStep();

        // When
        Job job = config.searchQueryLogFlushJob(step);

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("searchQueryLogFlushJob");
    }

    @Test
    @DisplayName("searchQueryLogFlushStep Bean 생성 성공")
    void searchQueryLogFlushStep_Created() {
        // When
        Step step = config.searchQueryLogFlushStep();

        // Then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("searchQueryLogFlushStep");
    }
}
