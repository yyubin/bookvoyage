package org.yyubin.recommendation.candidate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecommendationCandidate 테스트")
class RecommendationCandidateTest {

    @Test
    @DisplayName("Builder로 RecommendationCandidate 생성")
    void build_Success() {
        // Given & When
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(1L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE)
                .initialScore(0.8)
                .reason("Similar users also liked this book")
                .build();

        // Then
        assertThat(candidate.getBookId()).isEqualTo(1L);
        assertThat(candidate.getSource()).isEqualTo(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE);
        assertThat(candidate.getInitialScore()).isEqualTo(0.8);
        assertThat(candidate.getReason()).isEqualTo("Similar users also liked this book");
    }

    @Test
    @DisplayName("모든 CandidateSource 타입 테스트")
    void allCandidateSources() {
        // Given
        RecommendationCandidate.CandidateSource[] sources = RecommendationCandidate.CandidateSource.values();

        // Then
        assertThat(sources).contains(
                RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE,
                RecommendationCandidate.CandidateSource.NEO4J_GENRE,
                RecommendationCandidate.CandidateSource.NEO4J_AUTHOR,
                RecommendationCandidate.CandidateSource.NEO4J_TOPIC,
                RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC,
                RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT,
                RecommendationCandidate.CandidateSource.POPULARITY,
                RecommendationCandidate.CandidateSource.RECENT
        );
    }

    @Test
    @DisplayName("Setter로 값 변경")
    void setters_Work() {
        // Given
        RecommendationCandidate candidate = new RecommendationCandidate();

        // When
        candidate.setBookId(100L);
        candidate.setSource(RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT);
        candidate.setInitialScore(0.95);
        candidate.setReason("More like this");

        // Then
        assertThat(candidate.getBookId()).isEqualTo(100L);
        assertThat(candidate.getSource()).isEqualTo(RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT);
        assertThat(candidate.getInitialScore()).isEqualTo(0.95);
        assertThat(candidate.getReason()).isEqualTo("More like this");
    }

    @Test
    @DisplayName("Equals and HashCode 테스트")
    void equalsAndHashCode() {
        // Given
        RecommendationCandidate candidate1 = RecommendationCandidate.builder()
                .bookId(1L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.7)
                .reason("Same genre")
                .build();

        RecommendationCandidate candidate2 = RecommendationCandidate.builder()
                .bookId(1L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.7)
                .reason("Same genre")
                .build();

        RecommendationCandidate candidate3 = RecommendationCandidate.builder()
                .bookId(2L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.7)
                .reason("Same genre")
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
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(42L)
                .source(RecommendationCandidate.CandidateSource.POPULARITY)
                .initialScore(0.85)
                .reason("Popular book")
                .build();

        // When
        String result = candidate.toString();

        // Then
        assertThat(result).contains("42");
        assertThat(result).contains("POPULARITY");
        assertThat(result).contains("0.85");
        assertThat(result).contains("Popular book");
    }
}
