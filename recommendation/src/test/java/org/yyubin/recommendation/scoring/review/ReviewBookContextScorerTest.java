package org.yyubin.recommendation.scoring.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewBookContextScorer 테스트")
class ReviewBookContextScorerTest {

    @InjectMocks
    private ReviewBookContextScorer reviewBookContextScorer;

    @Test
    @DisplayName("bookContextId가 null이면 0 반환")
    void score_NullBookContextId_ReturnsZero() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .build();

        // When
        double score = reviewBookContextScorer.score(null, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("candidate의 bookId가 null이면 0 반환")
    void score_NullCandidateBookId_ReturnsZero() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(null)
                .build();

        // When
        double score = reviewBookContextScorer.score(100L, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("bookContextId와 candidate의 bookId가 같으면 1.0 반환")
    void score_MatchingBookIds_ReturnsOne() {
        // Given
        Long bookContextId = 100L;
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .build();

        // When
        double score = reviewBookContextScorer.score(bookContextId, candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("bookContextId와 candidate의 bookId가 다르면 0 반환")
    void score_DifferentBookIds_ReturnsZero() {
        // Given
        Long bookContextId = 100L;
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(200L)
                .build();

        // When
        double score = reviewBookContextScorer.score(bookContextId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("둘 다 null이면 0 반환")
    void score_BothNull_ReturnsZero() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(null)
                .build();

        // When
        double score = reviewBookContextScorer.score(null, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("같은 bookId - 경계값 테스트 (Long 값)")
    void score_SameBookIdEdgeCase_ReturnsOne() {
        // Given
        Long bookContextId = Long.MAX_VALUE;
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(Long.MAX_VALUE)
                .build();

        // When
        double score = reviewBookContextScorer.score(bookContextId, candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("다른 bookId - 경계값 테스트")
    void score_DifferentBookIdEdgeCase_ReturnsZero() {
        // Given
        Long bookContextId = Long.MAX_VALUE;
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(Long.MAX_VALUE - 1)
                .build();

        // When
        double score = reviewBookContextScorer.score(bookContextId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }
}
