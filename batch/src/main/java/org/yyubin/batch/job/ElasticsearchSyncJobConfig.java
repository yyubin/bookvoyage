package org.yyubin.batch.job;

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
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.recommendation.port.SearchBookPort;
import org.yyubin.batch.sync.BookSyncDataProvider;
import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.recommendation.search.document.BookDocument;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ElasticsearchSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final BatchProperties batchProperties;
    private final BookJpaRepository bookJpaRepository;
    private final SearchBookPort searchBookPort;
    private final BookSyncDataProvider bookSyncDataProvider;

    @Bean
    public Job elasticsearchSyncJob(Step syncBooksToElasticsearchStep) {
        return new JobBuilder("elasticsearchSyncJob", jobRepository)
                .start(syncBooksToElasticsearchStep)
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
                .build();
    }

    @Bean
    public RepositoryItemReader<BookEntity> bookReaderForElasticsearch() {
        return new RepositoryItemReaderBuilder<BookEntity>()
                .name("bookReaderForElasticsearch")
                .repository(bookJpaRepository)
                .methodName("findAll")
                .pageSize(batchProperties.getSync().getElasticsearch().getPageSize())
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<BookEntity, BookDocument> bookDocumentProcessor() {
        return entity -> {
            BookSyncDto dto = bookSyncDataProvider.build(entity);
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
    public ItemWriter<BookDocument> bookDocumentWriter() {
        return items -> {
            if (items.isEmpty()) {
                return;
            }
            log.info("Indexing {} books to Elasticsearch", items.size());
            searchBookPort.saveAll(items);
        };
    }
}
