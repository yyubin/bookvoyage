package org.yyubin.recommendation.scoring.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

/**
 * 리뷰 참여도 기반 스코어러
 * - 세션 부스트 해시를 활용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEngagementScorer {

    private final RedisTemplate<String, String> redisTemplate;

    public double score(Long userId, ReviewRecommendationCandidate candidate) {
        if (userId == null || candidate.getReviewId() == null) {
            return 0.0;
        }

        String key = "session:user:" + userId + ":reviews";
        String field = String.valueOf(candidate.getReviewId());

        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            if (value != null) {
                double boost = Double.parseDouble(value.toString());
                return Math.min(1.0, boost / 0.5);
            }
        } catch (Exception e) {
            log.warn("Failed to read session boost for user {} review {}", userId, candidate.getReviewId(), e);
        }

        return 0.0;
    }
}
