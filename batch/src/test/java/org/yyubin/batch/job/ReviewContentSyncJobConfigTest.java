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
import org.yyubin.domain.review.BookGenre;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
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
import org.yyubin.recommendation.review.HighlightReviewRecommendationService;
import org.yyubin.recommendation.review.RecommendationIngestCommand;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewContentSyncJobConfig 테스트")
class ReviewContentSyncJobConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private BatchProperties batchProperties;

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private ReviewHighlightJpaRepository reviewHighlightJpaRepository;

    @Mock
    private HighlightJpaRepository highlightJpaRepository;

    @Mock
    private ReviewKeywordJpaRepository reviewKeywordJpaRepository;

    @Mock
    private KeywordJpaRepository keywordJpaRepository;

    @Mock
    private HighlightReviewRecommendationService reviewRecommendationService;

    private ReviewContentSyncJobConfig config;

    @BeforeEach
    void setUp() {
        config = new ReviewContentSyncJobConfig(
                jobRepository,
                transactionManager,
                batchProperties,
                reviewJpaRepository,
                bookJpaRepository,
                reviewHighlightJpaRepository,
                highlightJpaRepository,
                reviewKeywordJpaRepository,
                keywordJpaRepository,
                reviewRecommendationService
        );
    }

    @Test
    @DisplayName("reviewContentSyncJob Bean 생성 성공")
    void reviewContentSyncJob_Created() {
        // Given
        Step step = mock(Step.class);

        // When
        Job job = config.reviewContentSyncJob(step);

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("reviewContentSyncJob");
    }

    @Test
    @DisplayName("reviewContentProcessor가 ReviewEntity를 RecommendationIngestCommand로 변환")
    void reviewContentProcessor_TransformsEntity() throws Exception {
        // Given
        ReviewEntity reviewEntity = mock(ReviewEntity.class);
        when(reviewEntity.getId()).thenReturn(1L);
        when(reviewEntity.getUserId()).thenReturn(10L);
        when(reviewEntity.getBookId()).thenReturn(100L);
        when(reviewEntity.getSummary()).thenReturn("Test Summary");
        when(reviewEntity.getContent()).thenReturn("Test Content");
        when(reviewEntity.getGenre()).thenReturn(BookGenre.FANTASY);
        when(reviewEntity.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(reviewEntity.getRating()).thenReturn(4);

        // Highlight mappings
        ReviewHighlightEntity highlightMapping = mock(ReviewHighlightEntity.class);
        ReviewHighlightEntity.ReviewHighlightKey highlightId = mock(ReviewHighlightEntity.ReviewHighlightKey.class);
        when(highlightMapping.getId()).thenReturn(highlightId);
        when(highlightId.getHighlightId()).thenReturn(1L);
        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of(highlightMapping));

        HighlightEntity highlight = mock(HighlightEntity.class);
        when(highlight.getRawValue()).thenReturn("Raw Highlight");
        when(highlight.getNormalizedValue()).thenReturn("normalized highlight");
        when(highlightJpaRepository.findByIdIn(List.of(1L))).thenReturn(List.of(highlight));

        // Keyword mappings
        ReviewKeywordEntity keywordMapping = mock(ReviewKeywordEntity.class);
        ReviewKeywordEntity.ReviewKeywordKey keywordId = mock(ReviewKeywordEntity.ReviewKeywordKey.class);
        when(keywordMapping.getId()).thenReturn(keywordId);
        when(keywordId.getKeywordId()).thenReturn(1L);
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of(keywordMapping));

        KeywordEntity keyword = mock(KeywordEntity.class);
        when(keyword.getRawValue()).thenReturn("keyword1");
        when(keywordJpaRepository.findByIdIn(List.of(1L))).thenReturn(List.of(keyword));

        // Book
        BookEntity book = mock(BookEntity.class);
        when(book.getTitle()).thenReturn("Test Book");
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.of(book));

        ItemProcessor<ReviewEntity, RecommendationIngestCommand> processor = config.reviewContentProcessor();

        // When
        RecommendationIngestCommand result = processor.process(reviewEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.reviewId()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(10L);
        assertThat(result.bookId()).isEqualTo(100L);
        assertThat(result.bookTitle()).isEqualTo("Test Book");
        assertThat(result.summary()).isEqualTo("Test Summary");
        assertThat(result.content()).isEqualTo("Test Content");
        assertThat(result.highlights()).containsExactly("Raw Highlight");
        assertThat(result.highlightsNorm()).containsExactly("normalized highlight");
        assertThat(result.keywords()).containsExactly("keyword1");
        assertThat(result.genre()).isEqualTo("FANTASY");
        assertThat(result.rating()).isEqualTo(4);
    }

    @Test
    @DisplayName("reviewContentProcessor가 하이라이트와 키워드 없이 처리")
    void reviewContentProcessor_WithoutHighlightsAndKeywords() throws Exception {
        // Given
        ReviewEntity reviewEntity = mock(ReviewEntity.class);
        when(reviewEntity.getId()).thenReturn(1L);
        when(reviewEntity.getUserId()).thenReturn(10L);
        when(reviewEntity.getBookId()).thenReturn(100L);
        when(reviewEntity.getSummary()).thenReturn("Summary");
        when(reviewEntity.getContent()).thenReturn("Content");
        when(reviewEntity.getGenre()).thenReturn(null);
        when(reviewEntity.getCreatedAt()).thenReturn(LocalDateTime.now());
        when(reviewEntity.getRating()).thenReturn(3);

        when(reviewHighlightJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(reviewKeywordJpaRepository.findByIdReviewId(1L)).thenReturn(List.of());
        when(bookJpaRepository.findById(100L)).thenReturn(Optional.empty());

        ItemProcessor<ReviewEntity, RecommendationIngestCommand> processor = config.reviewContentProcessor();

        // When
        RecommendationIngestCommand result = processor.process(reviewEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.bookTitle()).isNull();
        assertThat(result.highlights()).isEmpty();
        assertThat(result.highlightsNorm()).isEmpty();
        assertThat(result.keywords()).isEmpty();
        assertThat(result.genre()).isNull();
    }

    @Test
    @DisplayName("reviewContentWriter가 RecommendationIngestCommand 목록을 저장")
    void reviewContentWriter_IngestsCommands() throws Exception {
        // Given
        RecommendationIngestCommand cmd1 = new RecommendationIngestCommand(
                1L, 10L, 100L, "Book1", "Summary1", "Content1",
                List.of("h1"), List.of("h1_norm"), List.of("k1"),
                "FANTASY", LocalDateTime.now(), 4
        );
        RecommendationIngestCommand cmd2 = new RecommendationIngestCommand(
                2L, 20L, 200L, "Book2", "Summary2", "Content2",
                List.of("h2"), List.of("h2_norm"), List.of("k2"),
                "ROMANCE", LocalDateTime.now(), 5
        );
        List<RecommendationIngestCommand> commands = List.of(cmd1, cmd2);

        ItemWriter<RecommendationIngestCommand> writer = config.reviewContentWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(commands));

        // Then
        verify(reviewRecommendationService).ingest(cmd1);
        verify(reviewRecommendationService).ingest(cmd2);
    }

    @Test
    @DisplayName("reviewContentWriter가 빈 목록일 때 저장하지 않음")
    void reviewContentWriter_SkipsEmptyList() throws Exception {
        // Given
        ItemWriter<RecommendationIngestCommand> writer = config.reviewContentWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(List.of()));

        // Then
        verify(reviewRecommendationService, never()).ingest(any());
    }
}
