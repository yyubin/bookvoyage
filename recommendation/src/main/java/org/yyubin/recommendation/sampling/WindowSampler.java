package org.yyubin.recommendation.sampling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.WindowSamplingConfig;
import org.yyubin.recommendation.sampling.strategy.ShuffleStrategy;
import org.yyubin.recommendation.sampling.strategy.ShuffleStrategyFactory;
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
 * <p>
 * 리팩토링: 책임 연쇄 패턴 적용으로 전략 분리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WindowSampler {

    private final WindowSamplingConfig config;
    private final ShuffleStrategyFactory strategyFactory;

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
        currentIndex = applyTierSampling(
                recommendations,
                result,
                currentIndex,
                config.getTier1(),
                sessionRandom,
                "Tier 1"
        );

        // Tier 2: 균형 구간
        currentIndex = applyTierSampling(
                recommendations,
                result,
                currentIndex,
                config.getTier2(),
                sessionRandom,
                "Tier 2"
        );

        // Tier 3: 탐색 구간
        applyTierSampling(
                recommendations,
                result,
                currentIndex,
                config.getTier3(),
                sessionRandom,
                "Tier 3"
        );

        log.info("Sampling complete: {} items processed", result.size());
        return result;
    }

    /**
     * Tier별 샘플링 적용 (책임 연쇄 패턴)
     *
     * @param source 원본 리스트
     * @param target 결과 리스트
     * @param startIndex 시작 인덱스
     * @param tierConfig Tier 설정
     * @param random Random 인스턴스
     * @param tierName Tier 이름 (로깅용)
     * @return 다음 시작 인덱스
     */
    private int applyTierSampling(
            List<RecommendationResult> source,
            List<RecommendationResult> target,
            int startIndex,
            WindowSamplingConfig.TierConfig tierConfig,
            Random random,
            String tierName
    ) {
        // Tier 크기 계산
        int tierSize = Math.min(tierConfig.getSize(), source.size() - startIndex);
        if (tierSize <= 0) {
            return startIndex;
        }

        // Tier 추출
        List<RecommendationResult> tierItems = new ArrayList<>(
                source.subList(startIndex, startIndex + tierSize)
        );

        // 전략 선택 및 실행
        ShuffleStrategy strategy = strategyFactory.getStrategy(tierConfig.getStrategy());
        ShuffleStrategy.ShuffleConfig shuffleConfig = new ShuffleStrategy.ShuffleConfig(
                tierConfig.getFixedTopN(),
                tierConfig.getFixedTopN() // windowSize로도 사용
        );

        List<RecommendationResult> shuffled = strategy.shuffle(tierItems, shuffleConfig, random);
        target.addAll(shuffled);

        log.debug("{}: processed {} items with {} strategy",
                tierName, tierSize, tierConfig.getStrategy());

        return startIndex + tierSize;
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
