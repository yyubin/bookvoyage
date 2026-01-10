package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yyubin.batch.service.SearchQueryLogStreamFlusher;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SearchQueryLogFlushJobConfig {

    private final JobRepository jobRepository;
    private final SearchQueryLogStreamFlusher streamFlusher;

    @Bean
    public Job searchQueryLogFlushJob(Step searchQueryLogFlushStep) {
        return new JobBuilder("searchQueryLogFlushJob", jobRepository)
            .start(searchQueryLogFlushStep)
            .build();
    }

    @Bean
    public Step searchQueryLogFlushStep() {
        return new StepBuilder("searchQueryLogFlushStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                int flushed = streamFlusher.flush();
                if (flushed > 0) {
                    log.info("Flushed {} search query logs from Redis stream", flushed);
                }
                return RepeatStatus.FINISHED;
            })
            .build();
    }
}
