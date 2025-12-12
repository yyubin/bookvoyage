package org.yyubin.recommendation.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.RecommendationCandidate;

/**
 * 사용자 참여도 기반 스코어러
 * - 사용자의 최근 행동 패턴 반영
 * - Redis 세션 부스트 활용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EngagementScorer implements Scorer {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public double score(Long userId, RecommendationCandidate candidate) {
        // Redis에서 세션 부스트 조회
        String key = "session:user:" + userId + ":books";
        String field = String.valueOf(candidate.getBookId());

        try {
            Object value = redisTemplate.opsForHash().get(key, field);
            if (value != null) {
                double sessionBoost = Double.parseDouble(value.toString());
                // 세션 부스트를 0.0 ~ 1.0 범위로 정규화
                return Math.min(1.0, sessionBoost / 0.5); // 0.5 이상이면 1.0
            }
        } catch (Exception e) {
            log.warn("Failed to get session boost for user {} book {}", userId, candidate.getBookId(), e);
        }

        // 세션 부스트가 없으면 0점
        return 0.0;
    }

    @Override
    public String getName() {
        return "EngagementScorer";
    }

    @Override
    public double getDefaultWeight() {
        return 0.15; // 참여도 점수 가중치 15%
    }
}
