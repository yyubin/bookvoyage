package org.yyubin.recommendation.sampling.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 부분 셔플 전략
 * - 상위 N개는 고정
 * - 나머지는 랜덤 셔플
 */
@Slf4j
@Component
public class PartialShuffleStrategy implements ShuffleStrategy {

    @Override
    public List<RecommendationResult> shuffle(
            List<RecommendationResult> recommendations,
            ShuffleConfig config,
            Random random
    ) {
        if (recommendations.isEmpty()) {
            return new ArrayList<>();
        }

        int fixedCount = Math.min(config.fixedTopN(), recommendations.size());
        List<RecommendationResult> result = new ArrayList<>();

        // 1. 상위 N개 고정
        result.addAll(recommendations.subList(0, fixedCount));

        // 2. 나머지 셔플
        if (recommendations.size() > fixedCount) {
            List<RecommendationResult> shufflePart = new ArrayList<>(
                    recommendations.subList(fixedCount, recommendations.size())
            );
            Collections.shuffle(shufflePart, random);
            result.addAll(shufflePart);
        }

        log.debug("Partial shuffle: fixed top {}, shuffled {} items",
                fixedCount, recommendations.size() - fixedCount);

        return result;
    }
}
