package org.yyubin.recommendation.scoring.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewPopularityScorer 테스트")
class ReviewPopularityScorerTest {

    @InjectMocks
    private ReviewPopularityScorer reviewPopularityScorer;

    @Test
    @DisplayName("initialScore가 있으면 clamp된 값 반환")
    void score_WithInitialScore_ReturnsClampedValue() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .initialScore(0.75)
                .build();

        // When
        double score = reviewPopularityScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.75);
    }

    @Test
    @DisplayName("initialScore가 null이면 0.5 반환")
    void score_WithNullInitialScore_ReturnsDefault() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .initialScore(null)
                .build();

        // When
        double score = reviewPopularityScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.5);
    }

    @Test
    @DisplayName("initialScore가 1.0 초과면 1.0으로 clamp")
    void score_WithScoreOverOne_ReturnsClamped() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .initialScore(1.5)
                .build();

        // When
        double score = reviewPopularityScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("initialScore가 0 미만이면 0으로 clamp")
    void score_WithNegativeScore_ReturnsClamped() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .initialScore(-0.5)
                .build();

        // When
        double score = reviewPopularityScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("initialScore가 0이면 0 반환")
    void score_WithZeroScore_ReturnsZero() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .initialScore(0.0)
                .build();

        // When
        double score = reviewPopularityScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("initialScore가 1.0이면 1.0 반환")
    void score_WithExactlyOne_ReturnsOne() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .initialScore(1.0)
                .build();

        // When
        double score = reviewPopularityScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }
}
