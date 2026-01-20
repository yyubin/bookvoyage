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
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.search.document.ReviewDocument;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewElasticsearchCandidateGenerator 테스트")
class ReviewElasticsearchCandidateGeneratorTest {

    @Mock
    private ReviewDocumentRepository reviewDocumentRepository;

    @Mock
    private ReviewRecommendationProperties properties;

    @Mock
    private UserAnalysisContextPort userAnalysisContextPort;

    @InjectMocks
    private ReviewElasticsearchCandidateGenerator reviewElasticsearchCandidateGenerator;

    private Long userId;
    private ReviewRecommendationProperties.Search searchConfig;

    @BeforeEach
    void setUp() {
        userId = 1L;

        searchConfig = new ReviewRecommendationProperties.Search();
        searchConfig.setContextReviewLimit(5);
        searchConfig.setContextLibraryLimit(5);
        searchConfig.setContextSearchLimit(5);
        searchConfig.setContextSearchDays(30);
        searchConfig.setFeedInterestRatio(0.6);
        searchConfig.setFeedMltRatio(0.3);
        searchConfig.setFeedSemanticRatio(0.3);
        searchConfig.setSeedLimit(3);

        lenient().when(properties.getSearch()).thenReturn(searchConfig);
        lenient().when(properties.getMaxCandidates()).thenReturn(300);
    }

    @Test
    @DisplayName("피드 후보 생성 - 인기 리뷰")
    void generateFeedCandidates_PopularReviews() {
        // Given
        ReviewDocument review1 = ReviewDocument.builder()
                .id("1")
                .reviewId(100L)
                .bookId(1000L)
                .likeCount(50)
                .commentCount(10)
                .bookmarkCount(5)
                .viewCount(1000L)
                .createdAt(LocalDateTime.now())
                .build();

        ReviewDocument review2 = ReviewDocument.builder()
                .id("2")
                .reviewId(101L)
                .bookId(1001L)
                .likeCount(30)
                .commentCount(8)
                .bookmarkCount(3)
                .viewCount(800L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(review1, review2));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).anyMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.POPULARITY);
    }

    @Test
    @DisplayName("피드 후보 생성 - 부족하면 최신 리뷰로 보강")
    void generateFeedCandidates_FallbackToRecent() {
        // Given
        ReviewDocument recentReview = ReviewDocument.builder()
                .id("1")
                .reviewId(200L)
                .bookId(2000L)
                .likeCount(1)
                .commentCount(0)
                .bookmarkCount(0)
                .viewCount(10L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> emptyPopularPage = new PageImpl<>(Collections.emptyList());
        Page<ReviewDocument> recentPage = new PageImpl<>(List.of(recentReview));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(emptyPopularPage);
        when(reviewDocumentRepository.findAll(any(Pageable.class)))
                .thenReturn(recentPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).anyMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.RECENT);
    }

    @Test
    @DisplayName("피드 후보 생성 - 관심사 기반 (MLT)")
    void generateFeedCandidates_WithInterestMlt() {
        // Given
        ReviewSnapshot reviewSnapshot = new ReviewSnapshot(
                50L, 500L, "Book Title", List.of("Author"), 4, "Fiction", "Summary", List.of(), LocalDateTime.now()
        );
        UserAnalysisContext context = new UserAnalysisContext(
                userId,
                List.of(reviewSnapshot),
                List.of(),
                List.of()
        );

        ReviewDocument similarReview = ReviewDocument.builder()
                .id("1")
                .reviewId(300L)
                .bookId(3000L)
                .likeCount(20)
                .commentCount(5)
                .bookmarkCount(2)
                .viewCount(500L)
                .createdAt(LocalDateTime.now())
                .build();

        ReviewDocument popularReview = ReviewDocument.builder()
                .id("2")
                .reviewId(301L)
                .bookId(3001L)
                .likeCount(100)
                .commentCount(20)
                .bookmarkCount(10)
                .viewCount(2000L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(popularReview));
        Page<ReviewDocument> similarPage = new PageImpl<>(List.of(similarReview));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(context);
        when(reviewDocumentRepository.findSimilarReviews(anyString(), anyLong(), any(Pageable.class)))
                .thenReturn(similarPage);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);
        when(reviewDocumentRepository.searchByMultiMatch(anyString(), anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).anyMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW);
    }

    @Test
    @DisplayName("피드 후보 생성 - 관심사 기반 (시맨틱 검색)")
    void generateFeedCandidates_WithInterestSemantic() {
        // Given
        UserAnalysisContext context = new UserAnalysisContext(
                userId,
                List.of(),
                List.of(),
                List.of("SF 소설 추천")
        );

        ReviewDocument searchResult = ReviewDocument.builder()
                .id("1")
                .reviewId(400L)
                .bookId(4000L)
                .likeCount(15)
                .commentCount(3)
                .bookmarkCount(1)
                .viewCount(300L)
                .createdAt(LocalDateTime.now())
                .build();

        ReviewDocument popularReview = ReviewDocument.builder()
                .id("2")
                .reviewId(401L)
                .bookId(4001L)
                .likeCount(80)
                .commentCount(15)
                .bookmarkCount(8)
                .viewCount(1500L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(popularReview));
        Page<ReviewDocument> searchResultPage = new PageImpl<>(List.of(searchResult));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(context);
        when(reviewDocumentRepository.searchByMultiMatch(anyString(), anyLong(), any(Pageable.class)))
                .thenReturn(searchResultPage);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).anyMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW);
    }

    @Test
    @DisplayName("특정 도서 스코프 후보 생성")
    void generateBookScopedCandidates() {
        // Given
        Long bookId = 1000L;

        ReviewDocument bookReview1 = ReviewDocument.builder()
                .id("1")
                .reviewId(500L)
                .bookId(bookId)
                .likeCount(30)
                .commentCount(5)
                .bookmarkCount(2)
                .viewCount(600L)
                .createdAt(LocalDateTime.now())
                .build();

        ReviewDocument bookReview2 = ReviewDocument.builder()
                .id("2")
                .reviewId(501L)
                .bookId(bookId)
                .likeCount(25)
                .commentCount(4)
                .bookmarkCount(1)
                .viewCount(500L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> bookReviewsPage = new PageImpl<>(List.of(bookReview1, bookReview2));

        when(reviewDocumentRepository.findPublicReviewsByBook(eq(bookId), any(Pageable.class)))
                .thenReturn(bookReviewsPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateBookScopedCandidates(bookId, 10);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).allMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR);
        assertThat(candidates).extracting(ReviewRecommendationCandidate::getBookId)
                .containsOnly(bookId);
    }

    @Test
    @DisplayName("특정 도서 스코프 후보 생성 - 실패 시 빈 리스트")
    void generateBookScopedCandidates_FailureReturnsEmpty() {
        // Given
        Long bookId = 1000L;

        when(reviewDocumentRepository.findPublicReviewsByBook(eq(bookId), any(Pageable.class)))
                .thenThrow(new RuntimeException("ES connection failed"));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateBookScopedCandidates(bookId, 10);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("userId가 null이면 관심사 기반 생성 건너뜀")
    void generateFeedCandidates_NullUserId_SkipsInterest() {
        // Given
        ReviewDocument popularReview = ReviewDocument.builder()
                .id("1")
                .reviewId(600L)
                .bookId(6000L)
                .likeCount(50)
                .commentCount(10)
                .bookmarkCount(5)
                .viewCount(1000L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(popularReview));

        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(null, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).allMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.POPULARITY);
    }

    @Test
    @DisplayName("인기도 점수 계산")
    void calculatePopularityScore() {
        // Given
        ReviewDocument highEngagement = ReviewDocument.builder()
                .id("1")
                .reviewId(700L)
                .bookId(7000L)
                .likeCount(100)
                .commentCount(50)
                .bookmarkCount(30)
                .viewCount(5000L)
                .createdAt(LocalDateTime.now())
                .build();

        ReviewDocument lowEngagement = ReviewDocument.builder()
                .id("2")
                .reviewId(701L)
                .bookId(7001L)
                .likeCount(1)
                .commentCount(0)
                .bookmarkCount(0)
                .viewCount(10L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(highEngagement, lowEngagement));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);

        ReviewRecommendationCandidate highCandidate = candidates.stream()
                .filter(c -> c.getReviewId() == 700L)
                .findFirst()
                .orElseThrow();
        ReviewRecommendationCandidate lowCandidate = candidates.stream()
                .filter(c -> c.getReviewId() == 701L)
                .findFirst()
                .orElseThrow();

        assertThat(highCandidate.getInitialScore()).isGreaterThan(lowCandidate.getInitialScore());
    }

    @Test
    @DisplayName("인기도 점수 계산 - null 값 처리")
    void calculatePopularityScore_NullValues() {
        // Given
        ReviewDocument reviewWithNulls = ReviewDocument.builder()
                .id("1")
                .reviewId(800L)
                .bookId(8000L)
                .likeCount(null)
                .commentCount(null)
                .bookmarkCount(null)
                .viewCount(null)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(reviewWithNulls));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isNotNull();
    }

    @Test
    @DisplayName("컨텍스트 로드 실패 시 인기 리뷰만 반환")
    void generateFeedCandidates_ContextLoadFails() {
        // Given
        ReviewDocument popularReview = ReviewDocument.builder()
                .id("1")
                .reviewId(900L)
                .bookId(9000L)
                .likeCount(40)
                .commentCount(8)
                .bookmarkCount(4)
                .viewCount(800L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(popularReview));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenThrow(new RuntimeException("Context load failed"));
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).isNotEmpty();
        assertThat(candidates).allMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.POPULARITY);
    }

    @Test
    @DisplayName("maxCandidates 제한 적용")
    void generateFeedCandidates_MaxCandidatesLimit() {
        // Given
        when(properties.getMaxCandidates()).thenReturn(2);

        ReviewDocument review1 = createReviewDocument("1", 100L, 1000L, 50);
        ReviewDocument review2 = createReviewDocument("2", 101L, 1001L, 40);
        ReviewDocument review3 = createReviewDocument("3", 102L, 1002L, 30);

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(review1, review2, review3));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);
    }

    @Test
    @DisplayName("리뷰 문서의 reviewId가 null이면 id에서 파싱")
    void generateFeedCandidates_ReviewIdNull_ParseFromId() {
        // Given
        ReviewDocument reviewWithoutReviewId = ReviewDocument.builder()
                .id("12345")
                .reviewId(null)
                .bookId(1000L)
                .likeCount(20)
                .commentCount(5)
                .bookmarkCount(2)
                .viewCount(400L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ReviewDocument> popularPage = new PageImpl<>(List.of(reviewWithoutReviewId));

        when(userAnalysisContextPort.loadContext(anyLong(), anyInt(), anyInt(), anyInt(), any()))
                .thenReturn(null);
        when(reviewDocumentRepository.findByOrderByLikeCountDesc(any(Pageable.class)))
                .thenReturn(popularPage);

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewElasticsearchCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getReviewId()).isEqualTo(12345L);
    }

    private ReviewDocument createReviewDocument(String id, Long reviewId, Long bookId, int likeCount) {
        return ReviewDocument.builder()
                .id(id)
                .reviewId(reviewId)
                .bookId(bookId)
                .likeCount(likeCount)
                .commentCount(5)
                .bookmarkCount(2)
                .viewCount(500L)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
