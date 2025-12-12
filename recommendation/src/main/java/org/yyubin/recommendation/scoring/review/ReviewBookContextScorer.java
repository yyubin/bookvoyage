package org.yyubin.recommendation.scoring.review;

import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

/**
 * 도서 컨텍스트 보정 스코어러
 */
@Component
public class ReviewBookContextScorer {

    public double score(Long bookContextId, ReviewRecommendationCandidate candidate) {
        if (bookContextId == null || candidate.getBookId() == null) {
            return 0.0;
        }
        return bookContextId.equals(candidate.getBookId()) ? 1.0 : 0.0;
    }
}
