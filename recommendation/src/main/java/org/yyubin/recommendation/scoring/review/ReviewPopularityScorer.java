package org.yyubin.recommendation.scoring.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

/**
 * 리뷰 인기도 기반 스코어러
 */
@Slf4j
@Component
public class ReviewPopularityScorer {

    public double score(ReviewRecommendationCandidate candidate) {
        if (candidate.getInitialScore() != null) {
            return clamp(candidate.getInitialScore());
        }
        return 0.5;
    }

    private double clamp(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
