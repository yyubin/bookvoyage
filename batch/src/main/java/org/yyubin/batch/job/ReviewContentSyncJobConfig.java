package org.yyubin.batch.job;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.beans.factory.annotation.Value;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.highlight.HighlightEntity;
import org.yyubin.infrastructure.persistence.review.highlight.HighlightJpaRepository;
import org.yyubin.infrastructure.persistence.review.highlight.ReviewHighlightEntity;
import org.yyubin.infrastructure.persistence.review.highlight.ReviewHighlightJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.recommendation.review.RecommendationIngestCommand;
import org.yyubin.recommendation.review.ReviewRecommendationService;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReviewContentSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties batchProperties;
    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewHighlightJpaRepository reviewHighlightJpaRepository;
    private final HighlightJpaRepository highlightJpaRepository;
    private final ReviewKeywordJpaRepository reviewKeywordJpaRepository;
    private final KeywordJpaRepository keywordJpaRepository;
    private final ReviewRecommendationService reviewRecommendationService;

    @Bean
    public Job reviewContentSyncJob(Step reviewContentSyncStep) {
        return new JobBuilder("reviewContentSyncJob", jobRepository)
                .start(reviewContentSyncStep)
                .build();
    }

    @Bean
    public Step reviewContentSyncStep(
            ItemReader<ReviewEntity> reviewContentReader,
            ItemProcessor<ReviewEntity, RecommendationIngestCommand> reviewContentProcessor,
            ItemWriter<RecommendationIngestCommand> reviewContentWriter
    ) {
        int chunkSize = batchProperties.getRecommendation().getChunkSize();
        return new StepBuilder("reviewContentSyncStep", jobRepository)
                .<ReviewEntity, RecommendationIngestCommand>chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(reviewContentReader)
                .processor(reviewContentProcessor)
                .writer(reviewContentWriter)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<ReviewEntity> reviewContentReader(
            @Value("#{jobParameters['lastRunAtEpochMs']}") Long lastRunAtEpochMs
    ) {
        long epochMs = lastRunAtEpochMs != null ? lastRunAtEpochMs : 0L;
        java.time.LocalDateTime updatedAfter = java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(epochMs),
                java.time.ZoneOffset.UTC
        );
        return new RepositoryItemReaderBuilder<ReviewEntity>()
                .name("reviewContentReader")
                .repository(reviewJpaRepository)
                .methodName("findByUpdatedAtAfterOrderByIdAsc")
                .arguments(List.of(updatedAfter))
                .pageSize(batchProperties.getSync().getElasticsearch().getPageSize())
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<ReviewEntity, RecommendationIngestCommand> reviewContentProcessor() {
        return entity -> {
            List<ReviewHighlightEntity> highlightMappings = reviewHighlightJpaRepository.findByIdReviewId(entity.getId());
            List<Long> highlightIds = highlightMappings.stream()
                    .map(mapping -> mapping.getId().getHighlightId())
                    .toList();

            List<HighlightEntity> highlights = highlightIds.isEmpty()
                    ? List.of()
                    : highlightJpaRepository.findByIdIn(highlightIds);

            List<String> highlightRaw = highlights.stream()
                    .map(HighlightEntity::getRawValue)
                    .toList();
            List<String> highlightNorm = highlights.stream()
                    .map(HighlightEntity::getNormalizedValue)
                    .toList();

            List<ReviewKeywordEntity> keywordMappings = reviewKeywordJpaRepository.findByIdReviewId(entity.getId());
            List<Long> keywordIds = keywordMappings.stream()
                    .map(mapping -> mapping.getId().getKeywordId())
                    .toList();

            List<KeywordEntity> keywords = keywordIds.isEmpty()
                    ? List.of()
                    : keywordJpaRepository.findByIdIn(keywordIds);

            List<String> keywordRaw = keywords.stream()
                    .map(KeywordEntity::getRawValue)
                    .toList();

            return new RecommendationIngestCommand(
                    entity.getId(),
                    entity.getUserId(),
                    entity.getBookId(),
                    entity.getSummary(),
                    entity.getContent(),
                    highlightRaw,
                    highlightNorm,
                    keywordRaw,
                    entity.getGenre() != null ? entity.getGenre().name() : null,
                    entity.getCreatedAt(),
                    entity.getRating()
            );
        };
    }

    @Bean
    public ItemWriter<RecommendationIngestCommand> reviewContentWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }
            log.info("Syncing {} review contents to recommendation stores", items.size());
            for (RecommendationIngestCommand command : items) {
                reviewRecommendationService.ingest(command);
            }
        };
    }
}
