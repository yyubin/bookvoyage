package org.yyubin.recommendation.sampling.strategy;

import org.yyubin.recommendation.service.RecommendationResult;

import java.util.List;
import java.util.Random;

/**
 * 셔플 전략 인터페이스
 * <p>
 * 책임 연쇄 패턴을 사용하여 각 Tier별 셔플 전략을 분리
 */
public interface ShuffleStrategy {

    /**
     * 추천 리스트를 셔플
     *
     * @param recommendations 원본 추천 리스트
     * @param config 설정 정보 (fixedTopN 등)
     * @param random Random 인스턴스 (세션 기반 시드)
     * @return 셔플된 추천 리스트
     */
    List<RecommendationResult> shuffle(
            List<RecommendationResult> recommendations,
            ShuffleConfig config,
            Random random
    );

    /**
     * 셔플 설정
     */
    record ShuffleConfig(
            int fixedTopN,     // 고정할 상위 N개 (PARTIAL, WINDOW에서 사용)
            int windowSize     // 윈도우 크기 (WINDOW에서 사용)
    ) {
        public ShuffleConfig(int fixedTopN) {
            this(fixedTopN, fixedTopN);
        }
    }
}
