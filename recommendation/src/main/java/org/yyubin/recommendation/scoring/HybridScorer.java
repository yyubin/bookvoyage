package org.yyubin.recommendation.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.config.RecommendationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 하이브리드 스코어러
 * - 여러 스코어러의 점수를 가중치 기반으로 통합
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HybridScorer {

    private final GraphScorer graphScorer;
    private final SemanticScorer semanticScorer;
    private final EngagementScorer engagementScorer;
    private final PopularityScorer popularityScorer;
    private final FreshnessScorer freshnessScorer;
    private final RecommendationProperties properties;

    /**
     * 최종 점수 계산
     *
     * @param userId 사용자 ID
     * @param candidate 후보
     * @return 최종 점수 (0.0 ~ 1.0)
     */
    public double calculateFinalScore(Long userId, RecommendationCandidate candidate) {
        // 각 스코어러의 점수 계산
        double graphScore = graphScorer.score(userId, candidate);
        double semanticScore = semanticScorer.score(userId, candidate);
        double engagementScore = engagementScorer.score(userId, candidate);
        double popularityScore = popularityScorer.score(userId, candidate);
        double freshnessScore = freshnessScorer.score(userId, candidate);

        // 가중치 적용
        var weights = properties.getScoring().getWeights();
        double finalScore =
                graphScore * weights.getGraph() +
                        semanticScore * weights.getSemantic() +
                        engagementScore * weights.getEngagement() +
                        popularityScore * weights.getPopularity() +
                        freshnessScore * weights.getFreshness();

        log.trace("Scores for book {} (user {}): graph={}, semantic={}, engagement={}, popularity={}, freshness={}, final={}",
                candidate.getBookId(), userId, graphScore, semanticScore, engagementScore,
                popularityScore, freshnessScore, finalScore);

        return finalScore;
    }

    /**
     * 배치 스코어링
     *
     * @param userId 사용자 ID
     * @param candidates 후보 리스트
     * @return bookId -> finalScore 맵
     */
    public Map<Long, Double> batchCalculate(Long userId, List<RecommendationCandidate> candidates) {
        Map<Long, Double> scores = new HashMap<>();

        for (RecommendationCandidate candidate : candidates) {
            double finalScore = calculateFinalScore(userId, candidate);
            scores.put(candidate.getBookId(), finalScore);
        }

        log.debug("Calculated {} scores for user {}", scores.size(), userId);
        return scores;
    }

    /**
     * 스코어 상세 정보 (디버깅용)
     */
    public ScoreBreakdown getScoreBreakdown(Long userId, RecommendationCandidate candidate) {
        return ScoreBreakdown.builder()
                .bookId(candidate.getBookId())
                .graphScore(graphScorer.score(userId, candidate))
                .semanticScore(semanticScorer.score(userId, candidate))
                .engagementScore(engagementScorer.score(userId, candidate))
                .popularityScore(popularityScorer.score(userId, candidate))
                .freshnessScore(freshnessScorer.score(userId, candidate))
                .finalScore(calculateFinalScore(userId, candidate))
                .source(candidate.getSource())
                .reason(candidate.getReason())
                .build();
    }

    /**
     * 스코어 상세 정보 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class ScoreBreakdown {
        private Long bookId;
        private double graphScore;
        private double semanticScore;
        private double engagementScore;
        private double popularityScore;
        private double freshnessScore;
        private double finalScore;
        private RecommendationCandidate.CandidateSource source;
        private String reason;
    }
}
