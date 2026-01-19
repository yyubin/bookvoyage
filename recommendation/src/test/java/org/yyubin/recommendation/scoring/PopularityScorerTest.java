package org.yyubin.recommendation.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("PopularityScorer 테스트")
class PopularityScorerTest {

    @Mock
    private BookDocumentRepository bookDocumentRepository;

    @InjectMocks
    private PopularityScorer popularityScorer;

    private Long userId;

    @BeforeEach
    void setUp() {
        userId = 1L;
    }

    @Test
    @DisplayName("POPULARITY 소스 후보 - initialScore 반환")
    void score_PopularityCandidate_ReturnsInitialScore() {
        // Given
        double expectedScore = 0.75;
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.POPULARITY)
                .initialScore(expectedScore)
                .build();

        // When
        double score = popularityScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(expectedScore);
    }

    @ParameterizedTest
    @EnumSource(value = RecommendationCandidate.CandidateSource.class,
            names = {"NEO4J_COLLABORATIVE", "NEO4J_GENRE", "NEO4J_AUTHOR", "NEO4J_TOPIC",
                    "ELASTICSEARCH_SEMANTIC", "ELASTICSEARCH_MLT", "RECENT"})
    @DisplayName("비-POPULARITY 소스 후보 - 0점 반환")
    void score_NonPopularityCandidate_ReturnsZero(RecommendationCandidate.CandidateSource source) {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(source)
                .initialScore(0.9)
                .build();

        // When
        double score = popularityScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("NEO4J_GENRE 후보 - 0점 반환")
    void score_Neo4jGenreCandidate_ReturnsZero() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.85)
                .build();

        // When
        double score = popularityScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
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
        double score = popularityScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("POPULARITY 후보 - 높은 initialScore")
    void score_PopularityCandidateWithHighScore_ReturnsHighScore() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.POPULARITY)
                .initialScore(0.95)
                .build();

        // When
        double score = popularityScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.95);
    }

    @Test
    @DisplayName("POPULARITY 후보 - 낮은 initialScore")
    void score_PopularityCandidateWithLowScore_ReturnsLowScore() {
        // Given
        RecommendationCandidate candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.POPULARITY)
                .initialScore(0.1)
                .build();

        // When
        double score = popularityScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.1);
    }

    @Test
    @DisplayName("getName은 PopularityScorer를 반환")
    void getName_ReturnsPopularityScorer() {
        // When
        String name = popularityScorer.getName();

        // Then
        assertThat(name).isEqualTo("PopularityScorer");
    }

    @Test
    @DisplayName("getDefaultWeight는 0.1을 반환")
    void getDefaultWeight_Returns01() {
        // When
        double weight = popularityScorer.getDefaultWeight();

        // Then
        assertThat(weight).isEqualTo(0.1);
    }
}
