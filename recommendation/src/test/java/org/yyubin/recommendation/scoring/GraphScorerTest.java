package org.yyubin.recommendation.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.RecommendationCandidate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("GraphScorer 테스트")
class GraphScorerTest {

    @InjectMocks
    private GraphScorer graphScorer;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @ParameterizedTest
    @EnumSource(value = RecommendationCandidate.CandidateSource.class,
            names = {"NEO4J_COLLABORATIVE", "NEO4J_GENRE", "NEO4J_AUTHOR", "NEO4J_TOPIC"})
    @DisplayName("그래프 기반 후보 - initialScore 반환")
    void score_GraphBasedCandidate_ReturnsInitialScore(RecommendationCandidate.CandidateSource source) {
        // Given
        double expectedScore = 0.85;
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(source)
                .initialScore(expectedScore)
                .build();

        // When
        double score = graphScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(expectedScore);
    }

    @ParameterizedTest
    @EnumSource(value = RecommendationCandidate.CandidateSource.class,
            names = {"ELASTICSEARCH_SEMANTIC", "ELASTICSEARCH_MLT", "POPULARITY", "RECENT"})
    @DisplayName("비-그래프 기반 후보 - 0점 반환")
    void score_NonGraphBasedCandidate_ReturnsZero(RecommendationCandidate.CandidateSource source) {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(source)
                .initialScore(0.9)
                .build();

        // When
        double score = graphScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("NEO4J_COLLABORATIVE 후보 - initialScore 반환")
    void score_Neo4jCollaborativeCandidate_ReturnsInitialScore() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE)
                .initialScore(0.75)
                .build();

        // When
        double score = graphScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.75);
    }

    @Test
    @DisplayName("ELASTICSEARCH_SEMANTIC 후보 - 0점 반환")
    void score_ElasticsearchSemanticCandidate_ReturnsZero() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC)
                .initialScore(0.9)
                .build();

        // When
        double score = graphScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("initialScore가 null인 그래프 후보 - NullPointerException 발생")
    void score_GraphCandidateWithNullInitialScore_ThrowsNPE() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(null)
                .build();

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> graphScorer.score(userId, candidate));
    }

    @Test
    @DisplayName("getName은 GraphScorer를 반환")
    void getName_ReturnsGraphScorer() {
        // When
        String name = graphScorer.getName();

        // Then
        assertThat(name).isEqualTo("GraphScorer");
    }

    @Test
    @DisplayName("getDefaultWeight는 0.4를 반환")
    void getDefaultWeight_Returns04() {
        // When
        double weight = graphScorer.getDefaultWeight();

        // Then
        assertThat(weight).isEqualTo(0.4);
    }
}
