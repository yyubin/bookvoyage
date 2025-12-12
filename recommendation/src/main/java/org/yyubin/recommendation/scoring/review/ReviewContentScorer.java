package org.yyubin.recommendation.scoring.review;

import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

/**
 * 리뷰 콘텐츠/소스 기반 스코어러
 * - 소스 타입별 기본 가중을 부여
 */
@Component
public class ReviewContentScorer {

    public double score(ReviewRecommendationCandidate candidate) {
        if (candidate.getSource() == null) {
            return 0.5;
        }

        return switch (candidate.getSource()) {
            case SIMILAR_REVIEW -> 0.8;
            case FOLLOWED_USER -> 0.9;
            case BOOK_POPULAR -> 0.7;
            case POPULARITY -> 0.6;
            case RECENT -> 0.55;
        };
    }
}
