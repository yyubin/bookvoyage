package org.yyubin.recommendation.candidate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewRecommendationCandidate 테스트")
class ReviewRecommendationCandidateTest {

    @Test
    @DisplayName("Builder로 ReviewRecommendationCandidate 생성")
    void build_Success() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.POPULARITY)
                .initialScore(0.85)
                .reason("Trending review")
                .createdAt(now)
                .build();

        // Then
        assertThat(candidate.getReviewId()).isEqualTo(1L);
        assertThat(candidate.getBookId()).isEqualTo(100L);
        assertThat(candidate.getSource()).isEqualTo(ReviewRecommendationCandidate.CandidateSource.POPULARITY);
        assertThat(candidate.getInitialScore()).isEqualTo(0.85);
        assertThat(candidate.getReason()).isEqualTo("Trending review");
        assertThat(candidate.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("모든 CandidateSource 타입 테스트")
    void allCandidateSources() {
        // Given
        ReviewRecommendationCandidate.CandidateSource[] sources = ReviewRecommendationCandidate.CandidateSource.values();

        // Then
        assertThat(sources).contains(
                ReviewRecommendationCandidate.CandidateSource.POPULARITY,
                ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR,
                ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW,
                ReviewRecommendationCandidate.CandidateSource.FOLLOWED_USER,
                ReviewRecommendationCandidate.CandidateSource.RECENT,
                ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER,
                ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY
        );
    }

    @Test
    @DisplayName("Setter로 값 변경")
    void setters_Work() {
        // Given
        ReviewRecommendationCandidate candidate = new ReviewRecommendationCandidate();
        LocalDateTime now = LocalDateTime.now();

        // When
        candidate.setReviewId(50L);
        candidate.setBookId(200L);
        candidate.setSource(ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW);
        candidate.setInitialScore(0.75);
        candidate.setReason("Similar to your interests");
        candidate.setCreatedAt(now);

        // Then
        assertThat(candidate.getReviewId()).isEqualTo(50L);
        assertThat(candidate.getBookId()).isEqualTo(200L);
        assertThat(candidate.getSource()).isEqualTo(ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW);
        assertThat(candidate.getInitialScore()).isEqualTo(0.75);
        assertThat(candidate.getReason()).isEqualTo("Similar to your interests");
        assertThat(candidate.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Equals and HashCode 테스트")
    void equalsAndHashCode() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        ReviewRecommendationCandidate candidate1 = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR)
                .initialScore(0.7)
                .reason("Popular")
                .createdAt(now)
                .build();

        ReviewRecommendationCandidate candidate2 = ReviewRecommendationCandidate.builder()
                .reviewId(1L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR)
                .initialScore(0.7)
                .reason("Popular")
                .createdAt(now)
                .build();

        ReviewRecommendationCandidate candidate3 = ReviewRecommendationCandidate.builder()
                .reviewId(2L)
                .bookId(100L)
                .source(ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR)
                .initialScore(0.7)
                .reason("Popular")
                .createdAt(now)
                .build();

        // Then
        assertThat(candidate1).isEqualTo(candidate2);
        assertThat(candidate1).isNotEqualTo(candidate3);
        assertThat(candidate1.hashCode()).isEqualTo(candidate2.hashCode());
    }

    @Test
    @DisplayName("toString 테스트")
    void toString_ContainsAllFields() {
        // Given
        ReviewRecommendationCandidate candidate = ReviewRecommendationCandidate.builder()
                .reviewId(42L)
                .bookId(123L)
                .source(ReviewRecommendationCandidate.CandidateSource.FOLLOWED_USER)
                .initialScore(0.95)
                .reason("From a followed user")
                .build();

        // When
        String result = candidate.toString();

        // Then
        assertThat(result).contains("42");
        assertThat(result).contains("123");
        assertThat(result).contains("FOLLOWED_USER");
        assertThat(result).contains("0.95");
        assertThat(result).contains("From a followed user");
    }

    @Test
    @DisplayName("NoArgsConstructor로 생성 후 값 설정")
    void noArgsConstructor_ThenSetValues() {
        // Given
        ReviewRecommendationCandidate candidate = new ReviewRecommendationCandidate();

        // When
        candidate.setReviewId(10L);
        candidate.setSource(ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER);

        // Then
        assertThat(candidate.getReviewId()).isEqualTo(10L);
        assertThat(candidate.getSource()).isEqualTo(ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER);
        assertThat(candidate.getBookId()).isNull();
        assertThat(candidate.getInitialScore()).isNull();
    }

    @Test
    @DisplayName("AllArgsConstructor로 생성")
    void allArgsConstructor() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        ReviewRecommendationCandidate candidate = new ReviewRecommendationCandidate(
                5L, 50L,
                ReviewRecommendationCandidate.CandidateSource.RECENT,
                0.6, "Recent review", now
        );

        // Then
        assertThat(candidate.getReviewId()).isEqualTo(5L);
        assertThat(candidate.getBookId()).isEqualTo(50L);
        assertThat(candidate.getSource()).isEqualTo(ReviewRecommendationCandidate.CandidateSource.RECENT);
        assertThat(candidate.getInitialScore()).isEqualTo(0.6);
        assertThat(candidate.getReason()).isEqualTo("Recent review");
        assertThat(candidate.getCreatedAt()).isEqualTo(now);
    }
}
