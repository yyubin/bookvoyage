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
@DisplayName("SemanticScorer 테스트")
class SemanticScorerTest {

    @InjectMocks
    private SemanticScorer semanticScorer;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @ParameterizedTest
    @EnumSource(value = RecommendationCandidate.CandidateSource.class,
            names = {"ELASTICSEARCH_SEMANTIC", "ELASTICSEARCH_MLT"})
    @DisplayName("Elasticsearch 기반 후보 - initialScore 반환")
    void score_ElasticsearchBasedCandidate_ReturnsInitialScore(RecommendationCandidate.CandidateSource source) {
        // Given
        double expectedScore = 0.78;
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(source)
                .initialScore(expectedScore)
                .build();

        // When
        double score = semanticScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(expectedScore);
    }

    @ParameterizedTest
    @EnumSource(value = RecommendationCandidate.CandidateSource.class,
            names = {"NEO4J_COLLABORATIVE", "NEO4J_GENRE", "NEO4J_AUTHOR", "NEO4J_TOPIC", "POPULARITY", "RECENT"})
    @DisplayName("비-Elasticsearch 기반 후보 - 0점 반환")
    void score_NonElasticsearchBasedCandidate_ReturnsZero(RecommendationCandidate.CandidateSource source) {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(source)
                .initialScore(0.9)
                .build();

        // When
        double score = semanticScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("ELASTICSEARCH_SEMANTIC 후보 - initialScore 반환")
    void score_ElasticsearchSemanticCandidate_ReturnsInitialScore() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC)
                .initialScore(0.65)
                .build();

        // When
        double score = semanticScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.65);
    }

    @Test
    @DisplayName("ELASTICSEARCH_MLT 후보 - initialScore 반환")
    void score_ElasticsearchMltCandidate_ReturnsInitialScore() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT)
                .initialScore(0.82)
                .build();

        // When
        double score = semanticScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.82);
    }

    @Test
    @DisplayName("NEO4J_GENRE 후보 - 0점 반환")
    void score_Neo4jGenreCandidate_ReturnsZero() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.9)
                .build();

        // When
        double score = semanticScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("initialScore가 null인 Elasticsearch 후보 - NullPointerException 발생")
    void score_ElasticsearchCandidateWithNullInitialScore_ThrowsNPE() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC)
                .initialScore(null)
                .build();

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class,
                () -> semanticScorer.score(userId, candidate));
    }

    @Test
    @DisplayName("getName은 SemanticScorer를 반환")
    void getName_ReturnsSemanticScorer() {
        // When
        String name = semanticScorer.getName();

        // Then
        assertThat(name).isEqualTo("SemanticScorer");
    }

    @Test
    @DisplayName("getDefaultWeight는 0.3을 반환")
    void getDefaultWeight_Returns03() {
        // When
        double weight = semanticScorer.getDefaultWeight();

        // Then
        assertThat(weight).isEqualTo(0.3);
    }
}
