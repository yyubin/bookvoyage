package org.yyubin.recommendation.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.RecommendationCandidate;

/**
 * Neo4j 그래프 기반 스코어러
 * - 그래프 관계의 강도를 점수로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GraphScorer implements Scorer {

    @Override
    public double score(Long userId, RecommendationCandidate candidate) {
        // 그래프 기반 후보는 initialScore를 그대로 사용
        if (isGraphBasedCandidate(candidate)) {
            return candidate.getInitialScore();
        }

        // 그래프 기반이 아니면 0점
        return 0.0;
    }

    private boolean isGraphBasedCandidate(RecommendationCandidate candidate) {
        return switch (candidate.getSource()) {
            case NEO4J_COLLABORATIVE, NEO4J_GENRE, NEO4J_AUTHOR, NEO4J_TOPIC -> true;
            default -> false;
        };
    }

    @Override
    public String getName() {
        return "GraphScorer";
    }

    @Override
    public double getDefaultWeight() {
        return 0.4; // 그래프 점수 가중치 40%
    }
}
