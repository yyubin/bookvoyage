package org.yyubin.recommendation.candidate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort.ReviewSnapshot;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort.UserAnalysisContext;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort.UserBookSnapshot;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ElasticsearchCandidateGenerator 테스트")
class ElasticsearchCandidateGeneratorTest {

    @Mock
    private BookDocumentRepository bookDocumentRepository;

    @Mock
    private RecommendationProperties properties;

    @Mock
    private UserAnalysisContextPort userAnalysisContextPort;

    @InjectMocks
    private ElasticsearchCandidateGenerator elasticsearchCandidateGenerator;

    private Long userId;
    private RecommendationProperties.SearchConfig searchConfig;

    @BeforeEach
    void setUp() {
        userId = 1L;

        searchConfig = new RecommendationProperties.SearchConfig();
        searchConfig.setMaxCandidates(500);
        searchConfig.setMinScore(0.1);
        searchConfig.setContextReviewLimit(5);
        searchConfig.setContextLibraryLimit(5);
        searchConfig.setContextSearchLimit(5);
        searchConfig.setContextSearchDays(30);

        lenient().when(properties.getSearch()).thenReturn(searchConfig);
    }

    @Test
    @DisplayName("인기 도서 후보 생성")
    void generateCandidates_PopularBooks() {
        // Given
        BookDocument book1 = BookDocument.builder()
                .id("100")
                .title("Popular Book 1")
                .viewCount(1000)
                .wishlistCount(100)
                .reviewCount(50)
                .build();

        BookDocument book2 = BookDocument.builder()
                .id("101")
                .title("Popular Book 2")
                .viewCount(800)
                .wishlistCount(80)
                .reviewCount(40)
                .build();

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(book1, book2));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.POPULARITY);
        assertThat(candidates).extracting(RecommendationCandidate::getBookId)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("limit이 0이면 빈 리스트 반환")
    void generateCandidates_ZeroLimit_ReturnsEmpty() {
        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 0);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("limit이 음수이면 빈 리스트 반환")
    void generateCandidates_NegativeLimit_ReturnsEmpty() {
        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, -5);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("인기 도서가 없으면 빈 리스트 반환")
    void generateCandidates_NoPopularBooks_ReturnsEmpty() {
        // Given
        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of());

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("MLT 기반 후보 생성 - 시드 도서가 있는 경우")
    void generateCandidates_WithMltSeeds() {
        // Given
        BookDocument popularBook = BookDocument.builder()
                .id("100")
                .title("Popular Book")
                .viewCount(500)
                .wishlistCount(50)
                .reviewCount(20)
                .build();

        BookDocument similarBook = BookDocument.builder()
                .id("200")
                .title("Similar Book")
                .build();

        UserAnalysisContext context = new UserAnalysisContext(
                userId,
                List.of(new ReviewSnapshot(1L, 150L, "Seed Book", List.of("Author"), 4, "Fiction", "Summary", List.of(), LocalDateTime.now())),
                List.of(),
                List.of()
        );

        Page<BookDocument> similarBooksPage = new PageImpl<>(List.of(similarBook));

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(popularBook));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(context);
        when(bookDocumentRepository.findSimilarBooks(anyString(), any(Pageable.class)))
                .thenReturn(similarBooksPage);
        when(bookDocumentRepository.searchByMultiMatch(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).anyMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT);
    }

    @Test
    @DisplayName("시맨틱 검색 기반 후보 생성 - 검색어가 있는 경우")
    void generateCandidates_WithSemanticQueries() {
        // Given
        BookDocument popularBook = BookDocument.builder()
                .id("100")
                .title("Popular Book")
                .viewCount(500)
                .wishlistCount(50)
                .reviewCount(20)
                .build();

        BookDocument searchResultBook = BookDocument.builder()
                .id("300")
                .title("Search Result Book")
                .build();

        UserAnalysisContext context = new UserAnalysisContext(
                userId,
                List.of(),
                List.of(),
                List.of("추천 소설", "SF 소설")
        );

        Page<BookDocument> searchResultsPage = new PageImpl<>(List.of(searchResultBook));

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(popularBook));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(context);
        when(bookDocumentRepository.searchByMultiMatch(anyString(), any(Pageable.class)))
                .thenReturn(searchResultsPage);

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).anyMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC);
    }

    @Test
    @DisplayName("generateMoreLikeThisCandidates - 유사 도서 후보 생성")
    void generateMoreLikeThisCandidates() {
        // Given
        BookDocument similarBook1 = BookDocument.builder().id("201").title("Similar 1").build();
        BookDocument similarBook2 = BookDocument.builder().id("202").title("Similar 2").build();

        Page<BookDocument> similarBooksPage = new PageImpl<>(List.of(similarBook1, similarBook2));

        when(bookDocumentRepository.findSimilarBooks(eq("100"), any(Pageable.class)))
                .thenReturn(similarBooksPage);

        // When
        List<RecommendationCandidate> candidates =
                elasticsearchCandidateGenerator.generateMoreLikeThisCandidates(100L, 5);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT);
        assertThat(candidates).allMatch(c -> c.getInitialScore() == 0.7);
        assertThat(candidates.get(0).getReason()).contains("Similar to book 100");
    }

    @Test
    @DisplayName("generateSemanticSearchCandidates - 시맨틱 검색 후보 생성")
    void generateSemanticSearchCandidates() {
        // Given
        BookDocument searchResult1 = BookDocument.builder().id("301").title("Search Result 1").build();
        BookDocument searchResult2 = BookDocument.builder().id("302").title("Search Result 2").build();

        Page<BookDocument> searchResultsPage = new PageImpl<>(List.of(searchResult1, searchResult2));

        when(bookDocumentRepository.searchByMultiMatch(eq("추천 소설"), any(Pageable.class)))
                .thenReturn(searchResultsPage);

        // When
        List<RecommendationCandidate> candidates =
                elasticsearchCandidateGenerator.generateSemanticSearchCandidates("추천 소설", 5);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC);
        assertThat(candidates).allMatch(c -> c.getInitialScore() == 0.6);
        assertThat(candidates.get(0).getReason()).contains("Matched query: 추천 소설");
    }

    @Test
    @DisplayName("인기도 점수 계산 - viewCount, wishlistCount, reviewCount에 따라")
    void calculatePopularityScore() {
        // Given
        BookDocument highPopularityBook = BookDocument.builder()
                .id("100")
                .title("High Popularity")
                .viewCount(10000)
                .wishlistCount(500)
                .reviewCount(200)
                .build();

        BookDocument lowPopularityBook = BookDocument.builder()
                .id("101")
                .title("Low Popularity")
                .viewCount(10)
                .wishlistCount(1)
                .reviewCount(0)
                .build();

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(highPopularityBook, lowPopularityBook));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);

        RecommendationCandidate highCandidate = candidates.stream()
                .filter(c -> c.getBookId() == 100L)
                .findFirst()
                .orElseThrow();
        RecommendationCandidate lowCandidate = candidates.stream()
                .filter(c -> c.getBookId() == 101L)
                .findFirst()
                .orElseThrow();

        assertThat(highCandidate.getInitialScore()).isGreaterThan(lowCandidate.getInitialScore());
    }

    @Test
    @DisplayName("인기도 점수 계산 - null 값 처리")
    void calculatePopularityScore_NullValues() {
        // Given
        BookDocument bookWithNulls = BookDocument.builder()
                .id("100")
                .title("Book with nulls")
                .viewCount(null)
                .wishlistCount(null)
                .reviewCount(null)
                .build();

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(bookWithNulls));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isNotNull();
    }

    @Test
    @DisplayName("getSourceType 반환값 확인")
    void getSourceType_ReturnsSemantic() {
        // When
        RecommendationCandidate.CandidateSource sourceType = elasticsearchCandidateGenerator.getSourceType();

        // Then
        assertThat(sourceType).isEqualTo(RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC);
    }

    @Test
    @DisplayName("사용자 컨텍스트 로드 실패 시 인기 도서만 반환")
    void generateCandidates_ContextLoadFails_ReturnsPopularOnly() {
        // Given
        BookDocument popularBook = BookDocument.builder()
                .id("100")
                .title("Popular Book")
                .viewCount(500)
                .wishlistCount(50)
                .reviewCount(20)
                .build();

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(popularBook));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenThrow(new RuntimeException("Context load failed"));

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.POPULARITY);
    }

    @Test
    @DisplayName("userId가 null이면 컨텍스트 로드 건너뜀")
    void generateCandidates_NullUserId_SkipsContextLoad() {
        // Given
        BookDocument popularBook = BookDocument.builder()
                .id("100")
                .title("Popular Book")
                .viewCount(500)
                .wishlistCount(50)
                .reviewCount(20)
                .build();

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(popularBook));

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(null, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.POPULARITY);
    }

    @Test
    @DisplayName("MLT와 시맨틱 모두 시드가 없으면 인기도 limit 증가")
    void generateCandidates_NoSeeds_IncreasesPopularityLimit() {
        // Given
        BookDocument book1 = BookDocument.builder().id("100").title("Book 1").viewCount(100).wishlistCount(10).reviewCount(5).build();
        BookDocument book2 = BookDocument.builder().id("101").title("Book 2").viewCount(90).wishlistCount(9).reviewCount(4).build();
        BookDocument book3 = BookDocument.builder().id("102").title("Book 3").viewCount(80).wishlistCount(8).reviewCount(3).build();

        UserAnalysisContext emptyContext = new UserAnalysisContext(userId, List.of(), List.of(), List.of());

        when(bookDocumentRepository.findTop100ByOrderByViewCountDescWishlistCountDesc())
                .thenReturn(List.of(book1, book2, book3));
        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(emptyContext);

        // When
        List<RecommendationCandidate> candidates = elasticsearchCandidateGenerator.generateCandidates(userId, 10);

        // Then
        assertThat(candidates).allMatch(c ->
                c.getSource() == RecommendationCandidate.CandidateSource.POPULARITY);
    }
}
