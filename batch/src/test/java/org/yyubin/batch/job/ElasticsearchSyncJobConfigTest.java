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
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.yyubin.batch.config.BatchProperties;
import org.yyubin.batch.service.BatchBookSyncService;
import org.yyubin.batch.service.BatchReviewSyncService;
import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.batch.sync.ReviewEngagementStats;
import org.yyubin.batch.sync.ReviewEngagementStatsProvider;
import org.yyubin.batch.sync.ReviewSyncDto;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.recommendation.port.SearchBookPort;
import org.yyubin.recommendation.port.SearchReviewPort;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.document.ReviewDocument;
import org.yyubin.batch.listener.SyncTimestampListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElasticsearchSyncJobConfig 테스트")
class ElasticsearchSyncJobConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private BatchProperties batchProperties;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private SearchBookPort searchBookPort;

    @Mock
    private SearchReviewPort searchReviewPort;

    @Mock
    private BatchBookSyncService batchBookSyncService;

    @Mock
    private BatchReviewSyncService batchReviewSyncService;

    @Mock
    private ReviewViewCounterFlusher reviewViewCounterFlusher;

    @Mock
    private ReviewEngagementStatsProvider reviewEngagementStatsProvider;

    @Mock
    private SyncTimestampListener syncTimestampListener;

    private ElasticsearchSyncJobConfig config;

    @BeforeEach
    void setUp() {
        config = new ElasticsearchSyncJobConfig(
                jobRepository,
                transactionManager,
                batchProperties,
                bookJpaRepository,
                reviewJpaRepository,
                searchBookPort,
                searchReviewPort,
                batchBookSyncService,
                batchReviewSyncService,
                reviewViewCounterFlusher,
                reviewEngagementStatsProvider,
                syncTimestampListener
        );
    }

    @Test
    @DisplayName("elasticsearchSyncJob Bean 생성 성공")
    void elasticsearchSyncJob_Created() {
        // Given
        Step bookStep = mock(Step.class);
        Step reviewStep = mock(Step.class);
        Step flushStep = mock(Step.class);

        // When
        Job job = config.elasticsearchSyncJob(bookStep, reviewStep, flushStep);

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("elasticsearchSyncJob");
    }

    @Test
    @DisplayName("reviewViewFlushJob Bean 생성 성공")
    void reviewViewFlushJob_Created() {
        // Given
        Step flushStep = mock(Step.class);

        // When
        Job job = config.reviewViewFlushJob(flushStep);

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("reviewViewFlushJob");
    }

    @Test
    @DisplayName("flushReviewViewCountersStep Bean 생성 성공")
    void flushReviewViewCountersStep_Created() {
        // When
        Step step = config.flushReviewViewCountersStep();

        // Then
        assertThat(step).isNotNull();
        assertThat(step.getName()).isEqualTo("flushReviewViewCountersStep");
    }

    @Test
    @DisplayName("bookDocumentProcessor가 BookEntity를 BookDocument로 변환")
    void bookDocumentProcessor_TransformsEntity() throws Exception {
        // Given
        BookEntity bookEntity = mock(BookEntity.class);

        BookSyncDto dto = new BookSyncDto(
                1L,
                "Test Book",
                "1234567890",
                "Description",
                LocalDate.now(),
                List.of("Author1"),
                List.of("Fantasy"),
                List.of("Topic1"),
                100,
                50,
                10,
                4.5f
        );
        when(batchBookSyncService.buildSyncData(bookEntity)).thenReturn(dto);

        ItemProcessor<BookEntity, BookDocument> processor = config.bookDocumentProcessor();

        // When
        BookDocument result = processor.process(bookEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getIsbn()).isEqualTo("1234567890");
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getAverageRating()).isEqualTo(4.5f);
    }

    @Test
    @DisplayName("reviewDocumentProcessor가 ReviewEntity를 ReviewDocument로 변환")
    void reviewDocumentProcessor_TransformsEntity() throws Exception {
        // Given
        ReviewEntity reviewEntity = mock(ReviewEntity.class);

        ReviewSyncDto dto = new ReviewSyncDto(
                1L,
                10L,
                100L,
                "Test Book",
                "Summary",
                "Content",
                List.of("highlight1"),
                List.of("highlight_norm1"),
                List.of("keyword1"),
                4.0f,
                "PUBLIC",
                LocalDateTime.now(),
                5,
                3,
                2,
                10L,
                0.8f,
                "FANTASY"
        );
        when(batchReviewSyncService.buildSyncData(reviewEntity)).thenReturn(dto);

        ReviewEngagementStats stats = new ReviewEngagementStats(1000, 800, 150, 150000, 100);
        when(reviewEngagementStatsProvider.getStats(1L)).thenReturn(stats);

        ItemProcessor<ReviewEntity, ReviewDocument> processor = config.reviewDocumentProcessor();

        // When
        ReviewDocument result = processor.process(reviewEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("1");
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getBookId()).isEqualTo(100L);
        assertThat(result.getBookTitle()).isEqualTo("Test Book");
    }

    @Test
    @DisplayName("bookDocumentWriter가 BookDocument 목록을 저장")
    void bookDocumentWriter_SavesDocuments() throws Exception {
        // Given
        BookDocument doc1 = BookDocument.builder().id("1").title("Book 1").build();
        BookDocument doc2 = BookDocument.builder().id("2").title("Book 2").build();
        List<BookDocument> docs = List.of(doc1, doc2);

        ItemWriter<BookDocument> writer = config.bookDocumentWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(docs));

        // Then
        verify(searchBookPort).saveAll(anyIterable());
    }

    @Test
    @DisplayName("bookDocumentWriter가 빈 목록일 때 저장하지 않음")
    void bookDocumentWriter_SkipsEmptyList() throws Exception {
        // Given
        ItemWriter<BookDocument> writer = config.bookDocumentWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(List.of()));

        // Then
        verify(searchBookPort, never()).saveAll(anyIterable());
    }

    @Test
    @DisplayName("reviewDocumentWriter가 ReviewDocument 목록을 저장")
    void reviewDocumentWriter_SavesDocuments() throws Exception {
        // Given
        ReviewDocument doc1 = ReviewDocument.builder().id("1").build();
        ReviewDocument doc2 = ReviewDocument.builder().id("2").build();
        List<ReviewDocument> docs = List.of(doc1, doc2);

        ItemWriter<ReviewDocument> writer = config.reviewDocumentWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(docs));

        // Then
        verify(searchReviewPort).saveAll(anyIterable());
    }

    @Test
    @DisplayName("reviewDocumentWriter가 빈 목록일 때 저장하지 않음")
    void reviewDocumentWriter_SkipsEmptyList() throws Exception {
        // Given
        ItemWriter<ReviewDocument> writer = config.reviewDocumentWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(List.of()));

        // Then
        verify(searchReviewPort, never()).saveAll(anyIterable());
    }
}
