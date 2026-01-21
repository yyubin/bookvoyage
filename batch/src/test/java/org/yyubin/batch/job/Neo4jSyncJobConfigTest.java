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
import org.yyubin.batch.service.BatchUserSyncService;
import org.yyubin.batch.sync.BookSyncDto;
import org.yyubin.batch.sync.UserSyncDto;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;
import org.yyubin.recommendation.graph.node.BookNode;
import org.yyubin.recommendation.graph.node.UserNode;
import org.yyubin.recommendation.port.GraphBookPort;
import org.yyubin.recommendation.port.GraphReviewPort;
import org.yyubin.recommendation.port.GraphUserPort;
import org.yyubin.recommendation.review.graph.ReviewNode;
import org.yyubin.batch.listener.SyncTimestampListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Neo4jSyncJobConfig 테스트")
class Neo4jSyncJobConfigTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private PlatformTransactionManager transactionManager;

    @Mock
    private BatchProperties batchProperties;

    @Mock
    private UserJpaRepository userJpaRepository;

    @Mock
    private BookJpaRepository bookJpaRepository;

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @Mock
    private GraphUserPort graphUserPort;

    @Mock
    private GraphBookPort graphBookPort;

    @Mock
    private GraphReviewPort graphReviewPort;

    @Mock
    private BatchUserSyncService batchUserSyncService;

    @Mock
    private BatchBookSyncService batchBookSyncService;

    @Mock
    private SyncTimestampListener syncTimestampListener;

    private Neo4jSyncJobConfig config;

    @BeforeEach
    void setUp() {
        config = new Neo4jSyncJobConfig(
                jobRepository,
                transactionManager,
                batchProperties,
                userJpaRepository,
                bookJpaRepository,
                reviewJpaRepository,
                graphUserPort,
                graphBookPort,
                graphReviewPort,
                batchUserSyncService,
                batchBookSyncService,
                syncTimestampListener
        );
    }

    @Test
    @DisplayName("neo4jSyncJob Bean 생성 성공")
    void neo4jSyncJob_Created() {
        // Given
        Step bookStep = mock(Step.class);
        Step reviewStep = mock(Step.class);
        Step userStep = mock(Step.class);

        // When
        Job job = config.neo4jSyncJob(bookStep, reviewStep, userStep);

        // Then
        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("neo4jSyncJob");
    }

    @Test
    @DisplayName("bookNodeProcessor가 BookEntity를 BookNode로 변환")
    void bookNodeProcessor_TransformsEntity() throws Exception {
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

        ItemProcessor<BookEntity, BookNode> processor = config.bookNodeProcessor();

        // When
        BookNode result = processor.process(bookEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
        assertThat(result.getIsbn()).isEqualTo("1234567890");
    }

    @Test
    @DisplayName("userNodeProcessor가 UserEntity를 UserNode로 변환")
    void userNodeProcessor_TransformsEntity() throws Exception {
        // Given
        UserEntity userEntity = mock(UserEntity.class);

        UserSyncDto dto = new UserSyncDto(
                1L,
                "testuser",
                "test@example.com",
                LocalDateTime.now(),
                List.of(),
                List.of(),
                List.of()
        );
        when(batchUserSyncService.buildSyncData(userEntity)).thenReturn(dto);

        ItemProcessor<UserEntity, UserNode> processor = config.userNodeProcessor();

        // When
        UserNode result = processor.process(userEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("reviewNodeProcessor가 ReviewEntity를 ReviewNode로 변환")
    void reviewNodeProcessor_TransformsEntity() throws Exception {
        // Given
        ReviewEntity reviewEntity = mock(ReviewEntity.class);
        when(reviewEntity.getId()).thenReturn(1L);
        when(reviewEntity.getUserId()).thenReturn(10L);
        when(reviewEntity.getBookId()).thenReturn(100L);

        ItemProcessor<ReviewEntity, ReviewNode> processor = config.reviewNodeProcessor();

        // When
        ReviewNode result = processor.process(reviewEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getBookId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("bookNodeWriter가 BookNode 목록을 저장")
    void bookNodeWriter_SavesNodes() throws Exception {
        // Given
        BookNode node1 = BookNode.builder().id(1L).title("Book 1").build();
        BookNode node2 = BookNode.builder().id(2L).title("Book 2").build();
        List<BookNode> nodes = List.of(node1, node2);

        ItemWriter<BookNode> writer = config.bookNodeWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(nodes));

        // Then
        verify(graphBookPort).saveAll(anyIterable());
    }

    @Test
    @DisplayName("bookNodeWriter가 빈 목록일 때 저장하지 않음")
    void bookNodeWriter_SkipsEmptyList() throws Exception {
        // Given
        ItemWriter<BookNode> writer = config.bookNodeWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(List.of()));

        // Then
        verify(graphBookPort, never()).saveAll(anyIterable());
    }

    @Test
    @DisplayName("userNodeWriter가 UserNode 목록을 저장")
    void userNodeWriter_SavesNodes() throws Exception {
        // Given
        UserNode node1 = UserNode.builder().id(1L).username("user1").build();
        UserNode node2 = UserNode.builder().id(2L).username("user2").build();
        List<UserNode> nodes = List.of(node1, node2);

        ItemWriter<UserNode> writer = config.userNodeWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(nodes));

        // Then
        verify(graphUserPort).saveAll(anyIterable());
    }

    @Test
    @DisplayName("reviewNodeWriter가 ReviewNode 목록을 저장")
    void reviewNodeWriter_SavesNodes() throws Exception {
        // Given
        ReviewNode node1 = new ReviewNode(1L, 10L, 100L, Set.of());
        ReviewNode node2 = new ReviewNode(2L, 20L, 200L, Set.of());
        List<ReviewNode> nodes = List.of(node1, node2);

        ItemWriter<ReviewNode> writer = config.reviewNodeWriter();

        // When
        writer.write(new org.springframework.batch.infrastructure.item.Chunk<>(nodes));

        // Then
        verify(graphReviewPort).saveAll(anyIterable());
    }

    @Test
    @DisplayName("bookNodeProcessor가 null/빈 author 필터링")
    void bookNodeProcessor_FiltersNullAuthors() throws Exception {
        // Given
        BookEntity bookEntity = mock(BookEntity.class);
        BookSyncDto dto = new BookSyncDto(
                1L,
                "Test Book",
                "1234567890",
                "Description",
                LocalDate.now(),
                Arrays.asList("Author1", "", "  ", null),
                List.of("Fantasy"),
                List.of("Topic1"),
                100,
                50,
                10,
                4.5f
        );
        when(batchBookSyncService.buildSyncData(bookEntity)).thenReturn(dto);

        ItemProcessor<BookEntity, BookNode> processor = config.bookNodeProcessor();

        // When
        BookNode result = processor.process(bookEntity);

        // Then
        assertThat(result.getAuthors()).hasSize(1);
        assertThat(result.getAuthors().iterator().next().getName()).isEqualTo("Author1");
    }
}
