package org.yyubin.recommendation.scoring.review;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

/**
 * 리뷰 신선도 스코어러
 */
@Slf4j
@Component
public class ReviewFreshnessScorer {

    public double score(ReviewRecommendationCandidate candidate) {
        LocalDateTime createdAt = candidate.getCreatedAt();
        if (createdAt == null) {
            return 0.5;
        }

        try {
            long days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
            if (days <= 0) {
                return 1.0;
            }
            if (days < 30) {
                return 1.0 - (days / 30.0) * 0.5; // 한 달 이내 약한 감쇠
            }
            if (days < 180) {
                return 0.5 - ((days - 30) / 150.0) * 0.3; // 6개월까지 추가 감쇠
            }
            return 0.2;
        } catch (Exception e) {
            log.debug("Failed to calculate freshness for review {}", candidate.getReviewId(), e);
            return 0.5;
        }
    }
}
