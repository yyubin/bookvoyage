package org.yyubin.recommendation.scoring.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewContentScorer 테스트")
class ReviewContentScorerTest {

    @InjectMocks
    private ReviewContentScorer reviewContentScorer;

    @Test
    @DisplayName("source가 null이면 0.5 반환")
    void score_NullSource_ReturnsDefault() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .source(null)
                .build();

        // When
        double score = reviewContentScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.5);
    }

    @ParameterizedTest
    @MethodSource("sourceScoreProvider")
    @DisplayName("각 소스 타입별 점수 반환")
    void score_BySourceType_ReturnsExpectedScore(
            ReviewRecommendationCandidate.CandidateSource source, double expectedScore) {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .source(source)
                .build();

        // When
        double score = reviewContentScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(expectedScore);
    }

    static Stream<Arguments> sourceScoreProvider() {
        return Stream.of(
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW, 0.8),
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.FOLLOWED_USER, 0.9),
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.7),
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.6),
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.RECENT, 0.55),
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER, 0.85),
                Arguments.of(ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY, 0.75)
        );
    }

    @Test
    @DisplayName("FOLLOWED_USER 소스가 가장 높은 점수 (0.9)")
    void score_FollowedUser_ReturnsHighestScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .source(ReviewRecommendationCandidate.CandidateSource.FOLLOWED_USER)
                .build();

        // When
        double score = reviewContentScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.9);
    }

    @Test
    @DisplayName("GRAPH_SIMILAR_USER 소스 점수 (0.85)")
    void score_GraphSimilarUser_ReturnsScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .source(ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER)
                .build();

        // When
        double score = reviewContentScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.85);
    }

    @Test
    @DisplayName("SIMILAR_REVIEW 소스 점수 (0.8)")
    void score_SimilarReview_ReturnsScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .source(ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW)
                .build();

        // When
        double score = reviewContentScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.8);
    }

    @Test
    @DisplayName("RECENT 소스가 가장 낮은 점수 (0.55)")
    void score_Recent_ReturnsLowestScore() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .source(ReviewRecommendationCandidate.CandidateSource.RECENT)
                .build();

        // When
        double score = reviewContentScorer.score(candidate);

        // Then
        assertThat(score).isEqualTo(0.55);
    }
}
