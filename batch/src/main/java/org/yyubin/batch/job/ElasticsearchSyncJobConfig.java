package org.yyubin.batch.job;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.recommendation.port.SearchBookPort;
import org.yyubin.recommendation.port.SearchReviewPort;
import org.yyubin.batch.service.BatchBookSyncService;
import org.yyubin.batch.service.BatchReviewSyncService;
import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.batch.sync.ReviewSyncDto;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.document.ReviewDocument;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties batchProperties;
    private final BookJpaRepository bookJpaRepository;
    private final ReviewJpaRepository reviewJpaRepository;
    private final SearchBookPort searchBookPort;
    private final SearchReviewPort searchReviewPort;
    private final BatchBookSyncService batchBookSyncService;
    private final BatchReviewSyncService batchReviewSyncService;
    private final ReviewViewCounterFlusher reviewViewCounterFlusher;
    private final org.yyubin.batch.sync.ReviewEngagementStatsProvider reviewEngagementStatsProvider;
    private final org.yyubin.batch.listener.SyncTimestampListener syncTimestampListener;

    @Bean
    public Job elasticsearchSyncJob(
            Step syncBooksToElasticsearchStep,
            Step syncReviewsToElasticsearchStep,
            Step flushReviewViewCountersStep
    ) {
        return new JobBuilder("elasticsearchSyncJob", jobRepository)
                .start(syncBooksToElasticsearchStep)
                .next(syncReviewsToElasticsearchStep)
                .next(flushReviewViewCountersStep)
                .build();
    }

    @Bean
    public Job reviewViewFlushJob(Step flushReviewViewCountersStep) {
        return new JobBuilder("reviewViewFlushJob", jobRepository)
                .start(flushReviewViewCountersStep)
                .build();
    }

    @Bean
    public Step syncBooksToElasticsearchStep(
            ItemReader<BookEntity> bookReaderForElasticsearch,
            ItemProcessor<BookEntity, BookDocument> bookDocumentProcessor,
            ItemWriter<BookDocument> bookDocumentWriter
    ) {
        int chunkSize = batchProperties.getSync().getElasticsearch().getChunkSize();
        return new StepBuilder("syncBooksToElasticsearchStep", jobRepository)
                .<BookEntity, BookDocument>chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(bookReaderForElasticsearch)
                .processor(bookDocumentProcessor)
                .writer(bookDocumentWriter)
                .listener(syncTimestampListener)
                .build();
    }

    @Bean
    public Step syncReviewsToElasticsearchStep(
            ItemReader<ReviewEntity> reviewReaderForElasticsearch,
            ItemProcessor<ReviewEntity, ReviewDocument> reviewDocumentProcessor,
            ItemWriter<ReviewDocument> reviewDocumentWriter
    ) {
        int chunkSize = batchProperties.getSync().getElasticsearch().getChunkSize();
        return new StepBuilder("syncReviewsToElasticsearchStep", jobRepository)
                .<ReviewEntity, ReviewDocument>chunk(chunkSize)
                .transactionManager(transactionManager)
                .reader(reviewReaderForElasticsearch)
                .processor(reviewDocumentProcessor)
                .writer(reviewDocumentWriter)
                .listener(syncTimestampListener)
                .build();
    }

    @Bean
    public Step flushReviewViewCountersStep() {
        return new StepBuilder("flushReviewViewCountersStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    reviewViewCounterFlusher.flush();
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<BookEntity> bookReaderForElasticsearch(
            @Value("#{jobParameters['lastSyncTime']}") String lastSyncTimeParam
    ) {
        LocalDateTime lastSyncTime = parseLastSyncTime(lastSyncTimeParam);

        if (lastSyncTime == null) {
            // 초기 실행: 전체 동기화
            log.info("Running full sync for books to Elasticsearch (no lastSyncTime)");
            return new RepositoryItemReaderBuilder<BookEntity>()
                    .name("bookReaderForElasticsearch")
                    .repository(bookJpaRepository)
                    .methodName("findAll")
                    .pageSize(batchProperties.getSync().getElasticsearch().getPageSize())
                    .sorts(Map.of("id", Sort.Direction.ASC))
                    .build();
        } else {
            // 증분 동기화
            log.info("Running incremental sync for books to Elasticsearch (lastSyncTime: {})", lastSyncTime);
            return new RepositoryItemReaderBuilder<BookEntity>()
                    .name("bookReaderForElasticsearch")
                    .repository(bookJpaRepository)
                    .methodName("findByUpdatedAtAfterOrderByIdAsc")
                    .arguments(Arrays.asList(lastSyncTime))
                    .pageSize(batchProperties.getSync().getElasticsearch().getPageSize())
                    .sorts(Map.of("id", Sort.Direction.ASC))
                    .build();
        }
    }

    @Bean
    @StepScope
    public RepositoryItemReader<ReviewEntity> reviewReaderForElasticsearch(
            @Value("#{jobParameters['lastSyncTime']}") String lastSyncTimeParam
    ) {
        LocalDateTime lastSyncTime = parseLastSyncTime(lastSyncTimeParam);

        if (lastSyncTime == null) {
            // 초기 실행: 전체 동기화
            log.info("Running full sync for reviews to Elasticsearch (no lastSyncTime)");
            return new RepositoryItemReaderBuilder<ReviewEntity>()
                    .name("reviewReaderForElasticsearch")
                    .repository(reviewJpaRepository)
                    .methodName("findAll")
                    .pageSize(batchProperties.getSync().getElasticsearch().getPageSize())
                    .sorts(Map.of("id", Sort.Direction.ASC))
                    .build();
        } else {
            // 증분 동기화
            log.info("Running incremental sync for reviews to Elasticsearch (lastSyncTime: {})", lastSyncTime);
            return new RepositoryItemReaderBuilder<ReviewEntity>()
                    .name("reviewReaderForElasticsearch")
                    .repository(reviewJpaRepository)
                    .methodName("findByUpdatedAtAfterOrderByIdAsc")
                    .arguments(Arrays.asList(lastSyncTime))
                    .pageSize(batchProperties.getSync().getElasticsearch().getPageSize())
                    .sorts(Map.of("id", Sort.Direction.ASC))
                    .build();
        }
    }

    @Bean
    public ItemProcessor<BookEntity, BookDocument> bookDocumentProcessor() {
        return entity -> {
            BookSyncDto dto = batchBookSyncService.buildSyncData(entity);
            String searchableText = BookDocument.buildSearchableText(dto.title(), dto.description(), dto.authors());

            return BookDocument.builder()
                    .id(String.valueOf(dto.id()))
                    .title(dto.title())
                    .description(dto.description())
                    .isbn(dto.isbn())
                    .authors(dto.authors())
                    .genres(dto.genres())
                    .topics(dto.topics())
                    .publishedDate(dto.publishedDate())
                    .viewCount(dto.viewCount())
                    .wishlistCount(dto.wishlistCount())
                    .reviewCount(dto.reviewCount())
                    .averageRating(dto.averageRating())
                    .searchableText(searchableText)
                    .build();
        };
    }

    @Bean
    public ItemProcessor<ReviewEntity, ReviewDocument> reviewDocumentProcessor() {
        return entity -> {
            ReviewSyncDto dto = batchReviewSyncService.buildSyncData(entity);
            var engagement = reviewEngagementStatsProvider.getStats(dto.id());
            return ReviewDocument.builder()
                    .id(String.valueOf(dto.id()))
                    .userId(dto.userId())
                    .reviewId(dto.id())
                    .bookId(dto.bookId())
                    .bookTitle(dto.bookTitle())
                    .summary(dto.summary())
                    .content(dto.content())
                    .highlights(dto.highlights())
                    .highlightsNorm(dto.highlightsNorm())
                    .keywords(dto.keywords())
                    .rating(dto.rating())
                    .genre(dto.genre())
                    .visibility(dto.visibility())
                    .createdAt(dto.createdAt())
                    .likeCount(dto.likeCount())
                    .bookmarkCount(dto.bookmarkCount())
                    .commentCount(dto.commentCount())
                    .viewCount(dto.viewCount())
                    .dwellScore(dto.dwellScore())
                    .avgDwellMs(engagement.avgDwellMs())
                    .ctr(engagement.ctr())
                    .reachRate(engagement.reachRate())
                    .searchableText(ReviewDocument.buildSearchableText(dto.content()))
                    .build();
        };
    }

    @Bean
    public ItemWriter<BookDocument> bookDocumentWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }
            log.info("Indexing {} books to Elasticsearch", items.size());
            searchBookPort.saveAll(items);
        };
    }

    @Bean
    public ItemWriter<ReviewDocument> reviewDocumentWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }
            log.info("Indexing {} reviews to Elasticsearch", items.size());
            searchReviewPort.saveAll(items);
        };
    }

    /**
     * jobParameters에서 전달된 lastSyncTime을 LocalDateTime으로 파싱
     * null이거나 파싱 실패 시 null 반환 (전체 동기화 실행)
     */
    private LocalDateTime parseLastSyncTime(String lastSyncTimeParam) {
        if (lastSyncTimeParam == null || lastSyncTimeParam.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(lastSyncTimeParam);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse lastSyncTime '{}', falling back to full sync", lastSyncTimeParam);
            return null;
        }
    }
}
