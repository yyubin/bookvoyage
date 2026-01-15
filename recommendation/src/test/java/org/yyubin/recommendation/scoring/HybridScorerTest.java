package org.yyubin.recommendation.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.config.RecommendationProperties;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("HybridScorer 테스트")
class HybridScorerTest {

    @Mock
    private GraphScorer graphScorer;

    @Mock
    private SemanticScorer semanticScorer;

    @Mock
    private PopularityScorer popularityScorer;

    @Mock
    private FreshnessScorer freshnessScorer;

    @Mock
    private RecommendationProperties properties;

    @InjectMocks
    private HybridScorer hybridScorer;

    private Long userId;
    private RecommendationCandidate candidate;
    private RecommendationProperties.ScoringConfig.Weights weights;

    @BeforeEach
    void setUp() {
        userId = 1L;
        candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE)
                .initialScore(0.8)
                .reason("Test candidate")
                .build();

        // 기본 가중치 설정
        weights = new RecommendationProperties.ScoringConfig.Weights();
        weights.setGraph(0.4);
        weights.setSemantic(0.3);
        weights.setPopularity(0.1);
        weights.setFreshness(0.05);

        RecommendationProperties.ScoringConfig scoringConfig = new RecommendationProperties.ScoringConfig();
        scoringConfig.setWeights(weights);

        lenient().when(properties.getScoring()).thenReturn(scoringConfig);
    }

    @Test
    @DisplayName("최종 점수 계산 - 모든 스코어러가 1.0을 반환하면 최종 점수도 1.0")
    void calculateFinalScore_AllScorersReturnOne() {
        // Given
        when(graphScorer.score(anyLong(), any())).thenReturn(1.0);
        when(semanticScorer.score(anyLong(), any())).thenReturn(1.0);
        when(popularityScorer.score(anyLong(), any())).thenReturn(1.0);
        when(freshnessScorer.score(anyLong(), any())).thenReturn(1.0);

        // When
        double finalScore = hybridScorer.calculateFinalScore(userId, candidate);

        // Then
        // 0.4 + 0.3 + 0.1 + 0.05 = 0.85
        assertThat(finalScore).isEqualTo(0.85);
    }

    @Test
    @DisplayName("최종 점수 계산 - 모든 스코어러가 0.0을 반환하면 최종 점수도 0.0")
    void calculateFinalScore_AllScorersReturnZero() {
        // Given
        when(graphScorer.score(anyLong(), any())).thenReturn(0.0);
        when(semanticScorer.score(anyLong(), any())).thenReturn(0.0);
        when(popularityScorer.score(anyLong(), any())).thenReturn(0.0);
        when(freshnessScorer.score(anyLong(), any())).thenReturn(0.0);

        // When
        double finalScore = hybridScorer.calculateFinalScore(userId, candidate);

        // Then
        assertThat(finalScore).isEqualTo(0.0);
    }

    @Test
    @DisplayName("최종 점수 계산 - 가중치에 따라 계산됨")
    void calculateFinalScore_WeightedCorrectly() {
        // Given
        when(graphScorer.score(anyLong(), any())).thenReturn(0.8);      // 0.8 * 0.4 = 0.32
        when(semanticScorer.score(anyLong(), any())).thenReturn(0.6);   // 0.6 * 0.3 = 0.18
        when(popularityScorer.score(anyLong(), any())).thenReturn(0.7); // 0.7 * 0.1 = 0.07
        when(freshnessScorer.score(anyLong(), any())).thenReturn(0.9);  // 0.9 * 0.05 = 0.045

        // When
        double finalScore = hybridScorer.calculateFinalScore(userId, candidate);

        // Then
        // 0.32 + 0.18 + 0.07 + 0.045 = 0.615
        assertThat(finalScore).isEqualTo(0.615);
    }

    @Test
    @DisplayName("배치 계산 - 여러 후보의 점수를 한번에 계산")
    void batchCalculate_MultipleCandidates() {
        // Given
        RecommendationCandidate candidate1 = RecommendationCandidate.builder()
                .bookId(1L).source(RecommendationCandidate.CandidateSource.NEO4J_GENRE).initialScore(0.8).build();

        RecommendationCandidate candidate2 = RecommendationCandidate.builder()
                .bookId(2L).source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT).initialScore(0.7).build();

        RecommendationCandidate candidate3 = RecommendationCandidate.builder()
                .bookId(3L).source(RecommendationCandidate.CandidateSource.POPULARITY).initialScore(0.9).build();

        List<RecommendationCandidate> candidates = List.of(candidate1, candidate2, candidate3);

        // Mock 설정
        when(graphScorer.score(anyLong(), any())).thenReturn(0.8);
        when(semanticScorer.score(anyLong(), any())).thenReturn(0.6);
        when(popularityScorer.score(anyLong(), any())).thenReturn(0.7);
        when(freshnessScorer.score(anyLong(), any())).thenReturn(0.9);

        // When
        Map<Long, Double> scores = hybridScorer.batchCalculate(userId, candidates);

        // Then
        assertThat(scores).hasSize(3);
        assertThat(scores).containsKeys(1L, 2L, 3L);
        assertThat(scores.get(1L)).isEqualTo(0.615);
        assertThat(scores.get(2L)).isEqualTo(0.615);
        assertThat(scores.get(3L)).isEqualTo(0.615);
    }

    @Test
    @DisplayName("점수 상세 정보 조회")
    void getScoreBreakdown_ReturnsDetailedScores() {
        // Given
        when(graphScorer.score(anyLong(), any())).thenReturn(0.8);
        when(semanticScorer.score(anyLong(), any())).thenReturn(0.6);
        when(popularityScorer.score(anyLong(), any())).thenReturn(0.7);
        when(freshnessScorer.score(anyLong(), any())).thenReturn(0.9);

        // When
        HybridScorer.ScoreBreakdown breakdown = hybridScorer.getScoreBreakdown(userId, candidate);

        // Then
        assertThat(breakdown.getBookId()).isEqualTo(100L);
        assertThat(breakdown.getGraphScore()).isEqualTo(0.8);
        assertThat(breakdown.getSemanticScore()).isEqualTo(0.6);
        assertThat(breakdown.getPopularityScore()).isEqualTo(0.7);
        assertThat(breakdown.getFreshnessScore()).isEqualTo(0.9);
        assertThat(breakdown.getFinalScore()).isEqualTo(0.615);
        assertThat(breakdown.getSource()).isEqualTo(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE);
        assertThat(breakdown.getReason()).isEqualTo("Test candidate");
    }

    @Test
    @DisplayName("빈 배치 계산 - 빈 Map 반환")
    void batchCalculate_EmptyList() {
        // Given
        List<RecommendationCandidate> candidates = List.of();

        // When
        Map<Long, Double> scores = hybridScorer.batchCalculate(userId, candidates);

        // Then
        assertThat(scores).isEmpty();
    }

    @Test
    @DisplayName("그래프 점수가 높을 때 최종 점수에 큰 영향")
    void calculateFinalScore_HighGraphScoreHasLargeImpact() {
        // Given - 그래프 점수만 높고 나머지는 낮음
        when(graphScorer.score(anyLong(), any())).thenReturn(1.0);
        when(semanticScorer.score(anyLong(), any())).thenReturn(0.0);
        when(popularityScorer.score(anyLong(), any())).thenReturn(0.0);
        when(freshnessScorer.score(anyLong(), any())).thenReturn(0.0);

        // When
        double finalScore = hybridScorer.calculateFinalScore(userId, candidate);

        // Then
        // 1.0 * 0.4 = 0.4 (그래프 스코어의 가중치가 가장 큼)
        assertThat(finalScore).isEqualTo(0.4);
    }

    @Test
    @DisplayName("다른 가중치 설정으로 최종 점수 계산")
    void calculateFinalScore_WithDifferentWeights() {
        // Given - 가중치 변경
        RecommendationProperties.ScoringConfig.Weights newWeights = new RecommendationProperties.ScoringConfig.Weights();
        newWeights.setGraph(0.5);
        newWeights.setSemantic(0.2);
        newWeights.setPopularity(0.1);
        newWeights.setFreshness(0.1);

        RecommendationProperties.ScoringConfig newScoringConfig = new RecommendationProperties.ScoringConfig();
        newScoringConfig.setWeights(newWeights);

        when(properties.getScoring()).thenReturn(newScoringConfig);

        when(graphScorer.score(anyLong(), any())).thenReturn(1.0);      // 1.0 * 0.5 = 0.5
        when(semanticScorer.score(anyLong(), any())).thenReturn(1.0);   // 1.0 * 0.2 = 0.2
        when(popularityScorer.score(anyLong(), any())).thenReturn(1.0); // 1.0 * 0.1 = 0.1
        when(freshnessScorer.score(anyLong(), any())).thenReturn(1.0);  // 1.0 * 0.1 = 0.1

        // When
        double finalScore = hybridScorer.calculateFinalScore(userId, candidate);

        // Then
        // 0.5 + 0.2 + 0.1 + 0.1 = 0.9
        assertThat(finalScore).isCloseTo(0.9, within(0.0001));
    }
}
