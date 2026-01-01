package org.yyubin.recommendation.sampling.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 윈도우 셔플 전략
 * - 리스트를 윈도우 크기만큼 나눔
 * - 각 윈도우 내에서 독립적으로 셔플
 */
@Slf4j
@Component
public class WindowShuffleStrategy implements ShuffleStrategy {

    private static final int DEFAULT_WINDOW_SIZE = 8;

    @Override
    public List<RecommendationResult> shuffle(
            List<RecommendationResult> recommendations,
            ShuffleConfig config,
            Random random
    ) {
        if (recommendations.isEmpty()) {
            return new ArrayList<>();
        }

        int windowSize = config.windowSize() > 0 ? config.windowSize() : DEFAULT_WINDOW_SIZE;
        List<RecommendationResult> result = new ArrayList<>();
        int windowCount = 0;

        // 윈도우 단위로 분할하여 각각 셔플
        for (int i = 0; i < recommendations.size(); i += windowSize) {
            int endIdx = Math.min(i + windowSize, recommendations.size());
            List<RecommendationResult> window = new ArrayList<>(
                    recommendations.subList(i, endIdx)
            );
            Collections.shuffle(window, random);
            result.addAll(window);
            windowCount++;
        }

        log.debug("Window shuffle: {} items across {} windows (window size: {})",
                recommendations.size(), windowCount, windowSize);

        return result;
    }
}
