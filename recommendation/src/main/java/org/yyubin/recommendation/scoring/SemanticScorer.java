package org.yyubin.recommendation.scoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.candidate.RecommendationCandidate;

/**
 * Elasticsearch 시맨틱 검색 기반 스코어러
 * - 텍스트 유사도를 점수로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SemanticScorer implements Scorer {

    @Override
    public double score(Long userId, RecommendationCandidate candidate) {
        // Elasticsearch 기반 후보는 initialScore를 그대로 사용
        if (isElasticsearchBasedCandidate(candidate)) {
            return candidate.getInitialScore();
        }

        // Elasticsearch 기반이 아니면 0점
        return 0.0;
    }

    private boolean isElasticsearchBasedCandidate(RecommendationCandidate candidate) {
        return switch (candidate.getSource()) {
            case ELASTICSEARCH_SEMANTIC, ELASTICSEARCH_MLT -> true;
            default -> false;
        };
    }

    @Override
    public String getName() {
        return "SemanticScorer";
    }

    @Override
    public double getDefaultWeight() {
        return 0.3; // 시맨틱 점수 가중치 30%
    }
}
