package org.yyubin.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 윈도우 샘플링 전략 설정
 * <p>
 * 추천 결과를 Tier별로 나누어 차등적으로 샘플링하는 전략 설정
 * - Tier 1: 고품질 보장 구간 (최상위 추천)
 * - Tier 2: 균형 구간 (품질과 다양성 균형)
 * - Tier 3: 탐색 구간 (Long Tail 발견)
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "recommendation.sampling")
public class WindowSamplingConfig {

    /**
     * 샘플링 활성화 여부
     */
    private boolean enabled = true;

    /**
     * Tier 1: 고정 또는 최소 변화 구간
     */
    private TierConfig tier1 = new TierConfig(10, ShuffleStrategy.PARTIAL, 3);

    /**
     * Tier 2: 균형 구간
     */
    private TierConfig tier2 = new TierConfig(40, ShuffleStrategy.WINDOW, 8);

    /**
     * Tier 3: 완전 랜덤 구간
     */
    private TierConfig tier3 = new TierConfig(50, ShuffleStrategy.FULL, 0);

    @Getter
    @Setter
    public static class TierConfig {
        private int size;
        private ShuffleStrategy strategy;
        private int fixedTopN; // PARTIAL 전략에서 고정할 상위 N개, WINDOW 전략에서는 윈도우 크기

        public TierConfig() {
        }

        public TierConfig(int size, ShuffleStrategy strategy, int fixedTopN) {
            this.size = size;
            this.strategy = strategy;
            this.fixedTopN = fixedTopN;
        }
    }

    /**
     * 셔플 전략
     */
    public enum ShuffleStrategy {
        /**
         * 셔플 없음 - 원본 순서 유지
         */
        NONE,

        /**
         * 부분 셔플 - 상위 N개 고정, 나머지 셔플
         */
        PARTIAL,

        /**
         * 윈도우 셔플 - 구간을 윈도우로 나누어 각각 독립적으로 셔플
         */
        WINDOW,

        /**
         * 완전 셔플 - 전체 랜덤 셔플
         */
        FULL
    }
}
