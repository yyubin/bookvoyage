package org.yyubin.recommendation.scoring;

import org.yyubin.recommendation.candidate.RecommendationCandidate;

import java.util.List;

/**
 * 스코어링 인터페이스
 */
public interface Scorer {

    /**
     * 후보에 대한 점수 계산
     *
     * @param userId 사용자 ID
     * @param candidate 후보
     * @return 점수 (0.0 ~ 1.0)
     */
    double score(Long userId, RecommendationCandidate candidate);

    /**
     * 배치 스코어링
     *
     * @param userId 사용자 ID
     * @param candidates 후보 리스트
     * @return 점수 리스트 (candidates와 같은 순서)
     */
    default List<Double> batchScore(Long userId, List<RecommendationCandidate> candidates) {
        return candidates.stream()
                .map(candidate -> score(userId, candidate))
                .toList();
    }

    /**
     * 스코어러 이름
     */
    String getName();

    /**
     * 스코어러 가중치 (기본값)
     */
    default double getDefaultWeight() {
        return 0.2;
    }
}
