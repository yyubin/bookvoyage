package org.yyubin.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

/**
 * RDB → Elasticsearch 동기화 배치 Job
 * - Book 데이터를 Elasticsearch로 인덱싱
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties batchProperties;
    private final BookDocumentRepository bookDocumentRepository;

    /**
     * Elasticsearch 동기화 Job
     */
    @Bean
    public Job elasticsearchSyncJob(Step syncBooksToElasticsearchStep) {
        return new JobBuilder("elasticsearchSyncJob", jobRepository)
                .start(syncBooksToElasticsearchStep)
                // 향후 syncReviewsStep 추가 가능
                .build();
    }

    /**
     * Book 인덱싱 Step
     */
    @Bean
    public Step syncBooksToElasticsearchStep(
            ItemReader<BookSyncDto> bookReader,
            ItemProcessor<BookSyncDto, BookDocument> bookDocumentProcessor,
            ItemWriter<BookDocument> bookDocumentWriter) {

        int chunkSize = batchProperties.getSync().getElasticsearch().getChunkSize();

        return new StepBuilder("syncBooksToElasticsearchStep", jobRepository)
                .<BookSyncDto, BookDocument>chunk(chunkSize, transactionManager)
                .reader(bookReader)
                .processor(bookDocumentProcessor)
                .writer(bookDocumentWriter)
                .build();
    }

    /**
     * Book Reader (RDB에서 읽기)
     */
    @Bean
    public ItemReader<BookSyncDto> bookReader() {
        // TODO: 실제 Book Repository로 교체 필요
        return new ItemReader<BookSyncDto>() {
            private boolean read = false;

            @Override
            public BookSyncDto read() {
                if (!read) {
                    read = true;
                    log.info("Reading books from RDB (dummy implementation)");
                    return null;
                }
                return null;
            }
        };
    }

    /**
     * BookDocument Processor (BookSyncDto → BookDocument 변환)
     */
    @Bean
    public ItemProcessor<BookSyncDto, BookDocument> bookDocumentProcessor() {
        return bookDto -> {
            if (bookDto == null) {
                return null;
            }

            String searchableText = BookDocument.buildSearchableText(
                    bookDto.getTitle(),
                    bookDto.getDescription(),
                    bookDto.getAuthors()
            );

            return BookDocument.builder()
                    .id(String.valueOf(bookDto.getId()))
                    .title(bookDto.getTitle())
                    .isbn(bookDto.getIsbn())
                    .description(bookDto.getDescription())
                    .publishedDate(bookDto.getPublishedDate())
                    .authors(bookDto.getAuthors())
                    .genres(bookDto.getGenres())
                    .topics(bookDto.getTopics())
                    .viewCount(bookDto.getViewCount())
                    .wishlistCount(bookDto.getWishlistCount())
                    .reviewCount(bookDto.getReviewCount())
                    .searchableText(searchableText)
                    .build();
        };
    }

    /**
     * BookDocument Writer (Elasticsearch에 인덱싱)
     */
    @Bean
    public ItemWriter<BookDocument> bookDocumentWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }

            log.info("Indexing {} books to Elasticsearch", items.size());
            bookDocumentRepository.saveAll(items);
            log.info("Successfully indexed {} books", items.size());
        };
    }
}
