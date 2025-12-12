package org.yyubin.recommendation.scoring.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;

/**
 * 리뷰 하이브리드 스코어러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewHybridScorer {

    private final ReviewPopularityScorer popularityScorer;
    private final ReviewFreshnessScorer freshnessScorer;
    private final ReviewEngagementScorer engagementScorer;
    private final ReviewContentScorer contentScorer;
    private final ReviewBookContextScorer bookContextScorer;
    private final ReviewRecommendationProperties properties;

    public double calculateFinalScore(Long userId, Long bookContextId, ReviewRecommendationCandidate candidate) {
        double popularity = popularityScorer.score(candidate);
        double freshness = freshnessScorer.score(candidate);
        double engagement = engagementScorer.score(userId, candidate);
        double content = contentScorer.score(candidate);
        double bookContext = bookContextScorer.score(bookContextId, candidate);

        var w = properties.getScoring().getWeights();
        double finalScore =
                popularity * w.getPopularity() +
                        freshness * w.getFreshness() +
                        engagement * w.getEngagement() +
                        content * w.getContent() +
                        bookContext * w.getBookContext();

        log.trace("Review score for review {} (user {}): pop={}, fresh={}, engage={}, content={}, context={}, final={}",
                candidate.getReviewId(), userId, popularity, freshness, engagement, content, bookContext, finalScore);
        return finalScore;
    }

    public Map<Long, Double> batchCalculate(Long userId, Long bookContextId, List<ReviewRecommendationCandidate> candidates) {
        Map<Long, Double> scores = new HashMap<>();
        for (ReviewRecommendationCandidate candidate : candidates) {
            double score = calculateFinalScore(userId, bookContextId, candidate);
            if (candidate.getReviewId() != null) {
                scores.put(candidate.getReviewId(), score);
            }
        }
        return scores;
    }
}
