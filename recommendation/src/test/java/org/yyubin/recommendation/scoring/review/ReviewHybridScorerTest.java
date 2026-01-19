package org.yyubin.recommendation.scoring.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewHybridScorer 테스트")
class ReviewHybridScorerTest {

    @Mock
    private ReviewPopularityScorer popularityScorer;

    @Mock
    private ReviewFreshnessScorer freshnessScorer;

    @Mock
    private ReviewEngagementScorer engagementScorer;

    @Mock
    private ReviewContentScorer contentScorer;

    @Mock
    private ReviewBookContextScorer bookContextScorer;

    @Mock
    private ReviewRecommendationProperties properties;

    @InjectMocks
    private ReviewHybridScorer reviewHybridScorer;

    private Long userId;
    private Long bookContextId;
    private ReviewRecommendationCandidate candidate;
    private ReviewRecommendationProperties.Scoring.Weights weights;

    @BeforeEach
    void setUp() {
        userId = 1L;
        bookContextId = 100L;
        candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.POPULARITY)
                .initialScore(0.8)
                .createdAt(LocalDateTime.now())
                .build();

        weights = new ReviewRecommendationProperties.Scoring.Weights();
        weights.setPopularity(0.35);
        weights.setFreshness(0.15);
        weights.setEngagement(0.2);
        weights.setContent(0.2);
        weights.setBookContext(0.1);

        ReviewRecommendationProperties.Scoring scoring = new ReviewRecommendationProperties.Scoring();
        scoring.setWeights(weights);

        lenient().when(properties.getScoring()).thenReturn(scoring);
    }

    @Test
    @DisplayName("모든 스코어러가 1.0을 반환하면 최종 점수는 가중치 합계")
    void calculateFinalScore_AllScorersReturnOne() {
        // Given
        when(popularityScorer.score(any())).thenReturn(1.0);
        when(freshnessScorer.score(any())).thenReturn(1.0);
        when(engagementScorer.score(anyLong(), any())).thenReturn(1.0);
        when(contentScorer.score(any())).thenReturn(1.0);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(1.0);

        // When
        double finalScore = reviewHybridScorer.calculateFinalScore(userId, bookContextId, candidate);

        // Then
        // 0.35 + 0.15 + 0.2 + 0.2 + 0.1 = 1.0
        assertThat(finalScore).isCloseTo(1.0, within(0.001));
    }

    @Test
    @DisplayName("모든 스코어러가 0을 반환하면 최종 점수 0")
    void calculateFinalScore_AllScorersReturnZero() {
        // Given
        when(popularityScorer.score(any())).thenReturn(0.0);
        when(freshnessScorer.score(any())).thenReturn(0.0);
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.0);
        when(contentScorer.score(any())).thenReturn(0.0);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(0.0);

        // When
        double finalScore = reviewHybridScorer.calculateFinalScore(userId, bookContextId, candidate);

        // Then
        assertThat(finalScore).isEqualTo(0.0);
    }

    @Test
    @DisplayName("가중치에 따라 점수 계산")
    void calculateFinalScore_WeightedCorrectly() {
        // Given
        when(popularityScorer.score(any())).thenReturn(0.8);      // 0.8 * 0.35 = 0.28
        when(freshnessScorer.score(any())).thenReturn(0.6);       // 0.6 * 0.15 = 0.09
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.5); // 0.5 * 0.2 = 0.1
        when(contentScorer.score(any())).thenReturn(0.7);         // 0.7 * 0.2 = 0.14
        when(bookContextScorer.score(anyLong(), any())).thenReturn(1.0); // 1.0 * 0.1 = 0.1

        // When
        double finalScore = reviewHybridScorer.calculateFinalScore(userId, bookContextId, candidate);

        // Then
        // 0.28 + 0.09 + 0.1 + 0.14 + 0.1 = 0.71
        assertThat(finalScore).isCloseTo(0.71, within(0.001));
    }

    @Test
    @DisplayName("인기도 점수만 높을 때")
    void calculateFinalScore_OnlyPopularityHigh() {
        // Given
        when(popularityScorer.score(any())).thenReturn(1.0);
        when(freshnessScorer.score(any())).thenReturn(0.0);
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.0);
        when(contentScorer.score(any())).thenReturn(0.0);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(0.0);

        // When
        double finalScore = reviewHybridScorer.calculateFinalScore(userId, bookContextId, candidate);

        // Then
        assertThat(finalScore).isEqualTo(0.35);
    }

    @Test
    @DisplayName("배치 계산 - 여러 후보의 점수를 한번에 계산")
    void batchCalculate_MultipleCandidates() {
        // Given
        ReviewRecommendationCandidate candidate1 = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.POPULARITY)
                .build();

        ReviewRecommendationCandidate candidate2 = ReviewRecommendationCandidate.builder()
                .reviewId(2L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.RECENT)
                .build();

        ReviewRecommendationCandidate candidate3 = ReviewRecommendationCandidate.builder()
                .reviewId(3L)
                .bookId(200L)
                .source(ReviewRecommendationCandidate.CandidateSource.FOLLOWED_USER)
                .build();

        List<ReviewRecommendationCandidate> candidates = List.of(candidate1, candidate2, candidate3);

        when(popularityScorer.score(any())).thenReturn(0.7);
        when(freshnessScorer.score(any())).thenReturn(0.6);
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.5);
        when(contentScorer.score(any())).thenReturn(0.8);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(0.5);

        // When
        Map<Long, Double> scores = reviewHybridScorer.batchCalculate(userId, bookContextId, candidates);

        // Then
        assertThat(scores).hasSize(3);
        assertThat(scores).containsKeys(1L, 2L, 3L);
    }

    @Test
    @DisplayName("배치 계산 - 빈 리스트는 빈 맵 반환")
    void batchCalculate_EmptyList() {
        // Given
        List<ReviewRecommendationCandidate> candidates = List.of();

        // When
        Map<Long, Double> scores = reviewHybridScorer.batchCalculate(userId, bookContextId, candidates);

        // Then
        assertThat(scores).isEmpty();
    }

    @Test
    @DisplayName("배치 계산 - reviewId가 null인 후보는 제외")
    void batchCalculate_NullReviewIdExcluded() {
        // Given
        ReviewRecommendationCandidate candidateWithNullId = ReviewRecommendationCandidate.builder()
                .reviewId(null)
                .bookId(100L)
                .build();

        ReviewRecommendationCandidate candidateWithId = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .build();

        List<ReviewRecommendationCandidate> candidates = List.of(candidateWithNullId, candidateWithId);

        when(popularityScorer.score(any())).thenReturn(0.7);
        when(freshnessScorer.score(any())).thenReturn(0.6);
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.5);
        when(contentScorer.score(any())).thenReturn(0.8);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(0.5);

        // When
        Map<Long, Double> scores = reviewHybridScorer.batchCalculate(userId, bookContextId, candidates);

        // Then
        assertThat(scores).hasSize(1);
        assertThat(scores).containsKey(1L);
    }

    @Test
    @DisplayName("북 컨텍스트가 일치하면 bookContext 점수 반영")
    void calculateFinalScore_WithMatchingBookContext() {
        // Given
        when(popularityScorer.score(any())).thenReturn(0.5);
        when(freshnessScorer.score(any())).thenReturn(0.5);
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.5);
        when(contentScorer.score(any())).thenReturn(0.5);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(1.0); // 같은 책

        // When
        double finalScore = reviewHybridScorer.calculateFinalScore(userId, bookContextId, candidate);

        // Then
        // 0.5*0.35 + 0.5*0.15 + 0.5*0.2 + 0.5*0.2 + 1.0*0.1 = 0.175 + 0.075 + 0.1 + 0.1 + 0.1 = 0.55
        assertThat(finalScore).isCloseTo(0.55, within(0.001));
    }

    @Test
    @DisplayName("북 컨텍스트가 다르면 bookContext 점수 0")
    void calculateFinalScore_WithDifferentBookContext() {
        // Given
        when(popularityScorer.score(any())).thenReturn(0.5);
        when(freshnessScorer.score(any())).thenReturn(0.5);
        when(engagementScorer.score(anyLong(), any())).thenReturn(0.5);
        when(contentScorer.score(any())).thenReturn(0.5);
        when(bookContextScorer.score(anyLong(), any())).thenReturn(0.0); // 다른 책

        // When
        double finalScore = reviewHybridScorer.calculateFinalScore(userId, bookContextId, candidate);

        // Then
        // 0.5*0.35 + 0.5*0.15 + 0.5*0.2 + 0.5*0.2 + 0.0*0.1 = 0.175 + 0.075 + 0.1 + 0.1 + 0 = 0.45
        assertThat(finalScore).isCloseTo(0.45, within(0.001));
    }
}
