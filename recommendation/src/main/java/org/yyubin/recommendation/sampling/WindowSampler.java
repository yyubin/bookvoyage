package org.yyubin.recommendation.sampling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.WindowSamplingConfig;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.*;

/**
 * 윈도우 샘플링 전략 구현체
 * <p>
 * Netflix, Instagram 등에서 사용하는 추천 다양성 전략 구현
 * - 품질 보장: 상위 추천은 항상 노출
 * - 다양성 확보: 매 새로고침마다 다른 결과 제공
 * - 저비용: 기존 캐시 활용, 추가 쿼리 불필요
 * - 일관성: 같은 세션 내에서는 일관된 순서 유지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WindowSampler {

    private final WindowSamplingConfig config;

    /**
     * 추천 리스트에 윈도우 샘플링 적용
     *
     * @param recommendations 원본 추천 리스트 (점수순 정렬 필수)
     * @param sessionId 세션 ID (같은 세션 내 일관성 보장용)
     * @return 샘플링된 추천 리스트
     */
    public List<RecommendationResult> applySampling(
            List<RecommendationResult> recommendations,
            String sessionId
    ) {
        if (!config.isEnabled() || recommendations.isEmpty()) {
            log.debug("Sampling disabled or empty list, returning as-is");
            return recommendations;
        }

        log.info("Applying window sampling to {} recommendations (session: {})",
                recommendations.size(), sessionId);

        // 세션 기반 시드 설정 (같은 세션 = 같은 결과)
        Random sessionRandom = new Random(generateSeed(sessionId));

        List<RecommendationResult> result = new ArrayList<>();
        int currentIndex = 0;

        // Tier 1: 고품질 보장 구간
        currentIndex = applyTier1Sampling(
                recommendations,
                result,
                currentIndex,
                sessionRandom
        );

        // Tier 2: 균형 구간
        currentIndex = applyTier2Sampling(
                recommendations,
                result,
                currentIndex,
                sessionRandom
        );

        // Tier 3: 탐색 구간
        applyTier3Sampling(
                recommendations,
                result,
                currentIndex,
                sessionRandom
        );

        log.info("Sampling complete: {} items processed", result.size());
        return result;
    }

    /**
     * Tier 1 샘플링: 상위 N개는 고정, 나머지는 가벼운 셔플
     */
    private int applyTier1Sampling(
            List<RecommendationResult> source,
            List<RecommendationResult> target,
            int startIndex,
            Random random
    ) {
        int tier1Size = Math.min(config.getTier1().getSize(), source.size() - startIndex);
        if (tier1Size <= 0) return startIndex;

        List<RecommendationResult> tier1 = new ArrayList<>(
                source.subList(startIndex, startIndex + tier1Size)
        );

        switch (config.getTier1().getStrategy()) {
            case NONE -> {
                // 완전 고정
                target.addAll(tier1);
                log.debug("Tier 1: Fixed {} items", tier1Size);
            }
            case PARTIAL -> {
                // 상위 N개 고정, 나머지 셔플
                int fixedCount = Math.min(config.getTier1().getFixedTopN(), tier1.size());
                target.addAll(tier1.subList(0, fixedCount));

                if (tier1.size() > fixedCount) {
                    List<RecommendationResult> shufflePart = new ArrayList<>(
                            tier1.subList(fixedCount, tier1.size())
                    );
                    Collections.shuffle(shufflePart, random);
                    target.addAll(shufflePart);
                }
                log.debug("Tier 1: Fixed top {}, shuffled {} items", fixedCount, tier1.size() - fixedCount);
            }
            case FULL -> {
                // 전체 셔플
                Collections.shuffle(tier1, random);
                target.addAll(tier1);
                log.debug("Tier 1: Fully shuffled {} items", tier1Size);
            }
            case WINDOW -> {
                // Tier 1에서는 WINDOW를 PARTIAL처럼 처리
                int fixedCount = Math.min(config.getTier1().getFixedTopN(), tier1.size());
                target.addAll(tier1.subList(0, fixedCount));

                if (tier1.size() > fixedCount) {
                    List<RecommendationResult> shufflePart = new ArrayList<>(
                            tier1.subList(fixedCount, tier1.size())
                    );
                    Collections.shuffle(shufflePart, random);
                    target.addAll(shufflePart);
                }
                log.debug("Tier 1: Window strategy treated as PARTIAL - Fixed top {}, shuffled {} items",
                        fixedCount, tier1.size() - fixedCount);
            }
        }

        return startIndex + tier1Size;
    }

    /**
     * Tier 2 샘플링: 윈도우 단위로 나눠서 각각 셔플
     */
    private int applyTier2Sampling(
            List<RecommendationResult> source,
            List<RecommendationResult> target,
            int startIndex,
            Random random
    ) {
        int tier2Size = Math.min(config.getTier2().getSize(), source.size() - startIndex);
        if (tier2Size <= 0) return startIndex;

        List<RecommendationResult> tier2 = new ArrayList<>(
                source.subList(startIndex, startIndex + tier2Size)
        );

        if (config.getTier2().getStrategy() == WindowSamplingConfig.ShuffleStrategy.WINDOW) {
            // 윈도우 크기로 분할하여 각각 셔플
            int windowSize = config.getTier2().getFixedTopN(); // 여기서는 windowSize로 활용
            if (windowSize <= 0) windowSize = 8; // 기본값

            int windowCount = 0;
            for (int i = 0; i < tier2.size(); i += windowSize) {
                int endIdx = Math.min(i + windowSize, tier2.size());
                List<RecommendationResult> window = new ArrayList<>(tier2.subList(i, endIdx));
                Collections.shuffle(window, random);
                target.addAll(window);
                windowCount++;
            }
            log.debug("Tier 2: Window shuffled {} items across {} windows (window size: {})",
                    tier2Size, windowCount, windowSize);

        } else if (config.getTier2().getStrategy() == WindowSamplingConfig.ShuffleStrategy.FULL) {
            Collections.shuffle(tier2, random);
            target.addAll(tier2);
            log.debug("Tier 2: Fully shuffled {} items", tier2Size);

        } else if (config.getTier2().getStrategy() == WindowSamplingConfig.ShuffleStrategy.PARTIAL) {
            // PARTIAL 전략: 상위 N개 고정, 나머지 셔플
            int fixedCount = Math.min(config.getTier2().getFixedTopN(), tier2.size());
            target.addAll(tier2.subList(0, fixedCount));

            if (tier2.size() > fixedCount) {
                List<RecommendationResult> shufflePart = new ArrayList<>(
                        tier2.subList(fixedCount, tier2.size())
                );
                Collections.shuffle(shufflePart, random);
                target.addAll(shufflePart);
            }
            log.debug("Tier 2: Partial - Fixed top {}, shuffled {} items", fixedCount, tier2.size() - fixedCount);

        } else {
            // NONE: 셔플 없음
            target.addAll(tier2);
            log.debug("Tier 2: No shuffle {} items", tier2Size);
        }

        return startIndex + tier2Size;
    }

    /**
     * Tier 3 샘플링: 완전 랜덤
     */
    private void applyTier3Sampling(
            List<RecommendationResult> source,
            List<RecommendationResult> target,
            int startIndex,
            Random random
    ) {
        if (startIndex >= source.size()) return;

        List<RecommendationResult> tier3 = new ArrayList<>(
                source.subList(startIndex, source.size())
        );

        if (config.getTier3().getStrategy() == WindowSamplingConfig.ShuffleStrategy.FULL) {
            Collections.shuffle(tier3, random);
            log.debug("Tier 3: Fully shuffled {} items", tier3.size());
        } else if (config.getTier3().getStrategy() == WindowSamplingConfig.ShuffleStrategy.WINDOW) {
            // WINDOW 전략
            int windowSize = config.getTier3().getFixedTopN();
            if (windowSize <= 0) windowSize = 10; // 기본값

            int windowCount = 0;
            for (int i = 0; i < tier3.size(); i += windowSize) {
                int endIdx = Math.min(i + windowSize, tier3.size());
                List<RecommendationResult> window = new ArrayList<>(tier3.subList(i, endIdx));
                Collections.shuffle(window, random);
                target.addAll(window);
                windowCount++;
            }
            log.debug("Tier 3: Window shuffled {} items across {} windows", tier3.size(), windowCount);
            return;
        } else {
            log.debug("Tier 3: No shuffle {} items", tier3.size());
        }

        target.addAll(tier3);
    }

    /**
     * 세션 ID로부터 시드 생성
     * - 같은 세션 = 같은 시드 = 같은 샘플링 결과
     * - 다른 세션 = 다른 시드 = 다른 샘플링 결과
     * <p>
     * 1분 단위로 시드가 변경되어 같은 세션이라도 시간이 지나면 약간 다른 결과 제공
     */
    private long generateSeed(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return System.currentTimeMillis();
        }

        // sessionId를 long으로 변환 (해시코드 활용)
        // 1분 단위로 갱신하여 완전히 고정되지 않도록 함
        long timeComponent = System.currentTimeMillis() / 60000; // 1분 = 60000ms
        return sessionId.hashCode() ^ timeComponent;
    }
}
