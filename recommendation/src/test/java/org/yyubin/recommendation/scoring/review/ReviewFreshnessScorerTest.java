package org.yyubin.recommendation.scoring.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewFreshnessScorer 테스트")
class ReviewFreshnessScorerTest {

    @InjectMocks
    private ReviewFreshnessScorer reviewFreshnessScorer;

    @Test
    @DisplayName("createdAt이 null이면 0.5 반환")
    void score_NullCreatedAt_ReturnsDefault() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(null)
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("오늘 작성된 리뷰 - 1.0 반환")
    void score_TodayReview_ReturnsOne() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("15일 전 작성된 리뷰 - 약 0.75 반환")
    void score_FifteenDaysAgoReview_ReturnsMediumHighScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusDays(15))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        // 1.0 - (15/30) * 0.5 = 1.0 - 0.25 = 0.75
        assertThat(score).isCloseTo(0.75, within(0.01));
    }

    @Test
    @DisplayName("30일 전 작성된 리뷰 - 0.5 반환")
    void score_ThirtyDaysAgoReview_ReturnsHalf() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        // 1.0 - (30/30) * 0.5 = 0.5
        assertThat(score).isCloseTo(0.5, within(0.01));
    }

    @Test
    @DisplayName("90일 전 작성된 리뷰 - 약 0.38 반환")
    void score_NinetyDaysAgoReview_ReturnsLowerMediumScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusDays(90))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        // 0.5 - ((90-30)/150) * 0.3 = 0.5 - 0.12 = 0.38
        assertThat(score).isCloseTo(0.38, within(0.01));
    }

    @Test
    @DisplayName("180일 전 작성된 리뷰 - 0.2 반환")
    void score_SixMonthsAgoReview_ReturnsLowScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusDays(180))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        // 0.5 - ((180-30)/150) * 0.3 = 0.5 - 0.3 = 0.2
        assertThat(score).isCloseTo(0.2, within(0.01));
    }

    @Test
    @DisplayName("1년 전 작성된 리뷰 - 0.2 반환 (최소값)")
    void score_OneYearAgoReview_ReturnsMinimum() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusYears(1))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.2);
    }

    @Test
    @DisplayName("미래 날짜 리뷰 - 1.0 반환")
    void score_FutureDateReview_ReturnsOne() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().plusDays(10))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("경계값: 1일 전 작성된 리뷰")
    void score_OneDayAgoReview_ReturnsHighScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        // 1.0 - (1/30) * 0.5 ≈ 0.983
        assertThat(score).isGreaterThan(0.95);
        assertThat(score).isLessThan(1.0);
    }

    @Test
    @DisplayName("경계값: 31일 전 작성된 리뷰 (두 번째 구간 시작)")
    void score_ThirtyOneDaysAgoReview_StartsSecondDecay() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .createdAt(LocalDateTime.now().minusDays(31))
                .build();

        // When
        double score = reviewFreshnessScorer.score(candidate);

        // Then
        // 0.5 - ((31-30)/150) * 0.3 = 0.5 - 0.002 ≈ 0.498
        assertThat(score).isCloseTo(0.498, within(0.01));
    }
}
