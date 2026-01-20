package org.yyubin.recommendation.candidate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;
import org.yyubin.recommendation.review.graph.ReviewNode;
import org.yyubin.recommendation.review.graph.ReviewNodeRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewNeo4jCandidateGenerator 테스트")
class ReviewNeo4jCandidateGeneratorTest {

    @Mock
    private UserNodeRepository userNodeRepository;

    @Mock
    private ReviewNodeRepository reviewNodeRepository;

    @Mock
    private ReviewRecommendationProperties properties;

    @InjectMocks
    private ReviewNeo4jCandidateGenerator reviewNeo4jCandidateGenerator;

    private Long userId;
    private ReviewRecommendationProperties.Search searchConfig;

    @BeforeEach
    void setUp() {
        userId = 1L;

        searchConfig = new ReviewRecommendationProperties.Search();
        searchConfig.setGraphSimilarRatio(0.6);
        searchConfig.setGraphBookSeedLimit(20);

        lenient().when(properties.getSearch()).thenReturn(searchConfig);
    }

    @Test
    @DisplayName("피드 후보 생성 - 유사 사용자 기반")
    void generateFeedCandidates_BySimilarUsers() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 8L}, // reviewId, bookId, score
                new Object[]{101L, 1001L, 6L}
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).allMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER);
        assertThat(candidates).extracting(ReviewRecommendationCandidate::getReviewId)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("피드 후보 생성 - 도서 친화도 기반")
    void generateFeedCandidates_ByBookAffinity() {
        // Given
        List<Long> interactedBookIds = List.of(500L, 501L, 502L);

        ReviewNode review1 = new ReviewNode(200L, 2L, 500L, null);
        ReviewNode review2 = new ReviewNode(201L, 3L, 501L, null);

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(interactedBookIds);
        when(reviewNodeRepository.findReviewsByBookIds(eq(interactedBookIds), eq(userId), anyInt()))
                .thenReturn(List.of(review1, review2));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).allMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY);
        assertThat(candidates).extracting(ReviewRecommendationCandidate::getReviewId)
                .containsExactlyInAnyOrder(200L, 201L);
    }

    @Test
    @DisplayName("피드 후보 생성 - 유사 사용자 + 도서 친화도 혼합")
    void generateFeedCandidates_MixedSources() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 7L}
        );

        List<Long> interactedBookIds = List.of(500L);

        ReviewNode affinityReview = new ReviewNode(200L, 2L, 500L, null);

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(interactedBookIds);
        when(reviewNodeRepository.findReviewsByBookIds(eq(interactedBookIds), eq(userId), anyInt()))
                .thenReturn(List.of(affinityReview));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(2);
        assertThat(candidates).anyMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER);
        assertThat(candidates).anyMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY);
    }

    @Test
    @DisplayName("userId가 null이면 빈 리스트 반환")
    void generateFeedCandidates_NullUserId_ReturnsEmpty() {
        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(null, 10);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("limit이 0이면 빈 리스트 반환")
    void generateFeedCandidates_ZeroLimit_ReturnsEmpty() {
        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 0);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("limit이 음수이면 빈 리스트 반환")
    void generateFeedCandidates_NegativeLimit_ReturnsEmpty() {
        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, -5);

        // Then
        assertThat(candidates).isEmpty();
    }

    @Test
    @DisplayName("유사 사용자 기반 점수 정규화 - 5 이상이면 1.0")
    void generateFeedCandidates_SimilarUsers_ScoreNormalization_MaxScore() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 10L} // score >= 5 => 1.0
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("유사 사용자 기반 점수 정규화 - 3이면 0.6")
    void generateFeedCandidates_SimilarUsers_ScoreNormalization_PartialScore() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 3L} // score = 3 => 3/5 = 0.6
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(0.6);
    }

    @Test
    @DisplayName("유사 사용자 기반 점수 정규화 - null이면 0.5")
    void generateFeedCandidates_SimilarUsers_ScoreNormalization_NullScore() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, null} // score = null => 0.5
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("도서 친화도 기반 점수는 항상 0.6")
    void generateFeedCandidates_BookAffinity_FixedScore() {
        // Given
        List<Long> interactedBookIds = List.of(500L);
        ReviewNode review = new ReviewNode(200L, 2L, 500L, null);

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(interactedBookIds);
        when(reviewNodeRepository.findReviewsByBookIds(eq(interactedBookIds), eq(userId), anyInt()))
                .thenReturn(List.of(review));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(0.6);
        assertThat(candidates.get(0).getReason()).isEqualTo("Reviews from books you engaged with");
    }

    @Test
    @DisplayName("유사 사용자 쿼리 실패 시 빈 리스트로 진행")
    void generateFeedCandidates_SimilarUsersQueryFails() {
        // Given
        List<Long> interactedBookIds = List.of(500L);
        ReviewNode review = new ReviewNode(200L, 2L, 500L, null);

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenThrow(new RuntimeException("Query failed"));
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(interactedBookIds);
        when(reviewNodeRepository.findReviewsByBookIds(eq(interactedBookIds), eq(userId), anyInt()))
                .thenReturn(List.of(review));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getSource())
                .isEqualTo(ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY);
    }

    @Test
    @DisplayName("도서 친화도 쿼리 실패 시 빈 리스트로 진행")
    void generateFeedCandidates_BookAffinityQueryFails() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 5L}
        );
        List<Long> interactedBookIds = List.of(500L);

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(interactedBookIds);
        when(reviewNodeRepository.findReviewsByBookIds(eq(interactedBookIds), eq(userId), anyInt()))
                .thenThrow(new RuntimeException("Query failed"));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getSource())
                .isEqualTo(ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER);
    }

    @Test
    @DisplayName("상호작용한 도서가 없으면 도서 친화도 후보 없음")
    void generateFeedCandidates_NoInteractedBooks() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 5L}
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates).allMatch(c ->
                c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER);
    }

    @Test
    @DisplayName("reviewId가 null인 결과는 무시")
    void generateFeedCandidates_NullReviewIdIgnored() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{null, 1000L, 5L}, // reviewId = null, 무시됨
                new Object[]{100L, 1001L, 4L}
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getReviewId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("asLong 헬퍼 - 다양한 Number 타입 변환")
    void generateFeedCandidates_AsLongHelper_VariousTypes() {
        // Given
        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{Integer.valueOf(100), Long.valueOf(1000L), Double.valueOf(5.0)}
        );

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(Collections.emptyList());

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(1);
        assertThat(candidates.get(0).getReviewId()).isEqualTo(100L);
        assertThat(candidates.get(0).getBookId()).isEqualTo(1000L);
        assertThat(candidates.get(0).getInitialScore()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("graphSimilarRatio에 따른 limit 분배")
    void generateFeedCandidates_LimitDistribution() {
        // Given - graphSimilarRatio = 0.6이므로 limit 10 중 6개가 유사 사용자용
        searchConfig.setGraphSimilarRatio(0.6);

        List<Object[]> similarUserResults = createObjectArrayList(
                new Object[]{100L, 1000L, 5L},
                new Object[]{101L, 1001L, 4L},
                new Object[]{102L, 1002L, 3L}
        );

        List<Long> interactedBookIds = List.of(500L);
        ReviewNode review = new ReviewNode(200L, 2L, 500L, null);

        when(userNodeRepository.findReviewIdsBySimilarUsers(eq(userId), anyInt()))
                .thenReturn(similarUserResults);
        when(userNodeRepository.findInteractedBookIds(eq(userId), anyInt()))
                .thenReturn(interactedBookIds);
        when(reviewNodeRepository.findReviewsByBookIds(eq(interactedBookIds), eq(userId), anyInt()))
                .thenReturn(List.of(review));

        // When
        List<ReviewRecommendationCandidate> candidates =
                reviewNeo4jCandidateGenerator.generateFeedCandidates(userId, 10);

        // Then
        assertThat(candidates).hasSize(4);
        long similarUserCount = candidates.stream()
                .filter(c -> c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER)
                .count();
        long bookAffinityCount = candidates.stream()
                .filter(c -> c.getSource() == ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY)
                .count();

        assertThat(similarUserCount).isEqualTo(3);
        assertThat(bookAffinityCount).isEqualTo(1);
    }

    private List<Object[]> createObjectArrayList(Object[]... arrays) {
        List<Object[]> list = new ArrayList<>();
        for (Object[] arr : arrays) {
            list.add(arr);
        }
        return list;
    }
}
