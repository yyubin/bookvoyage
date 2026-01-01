package org.yyubin.recommendation.sampling.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 완전 셔플 전략
 * - 전체 리스트를 랜덤하게 셔플
 */
@Slf4j
@Component
public class FullShuffleStrategy implements ShuffleStrategy {

    @Override
    public List<RecommendationResult> shuffle(
            List<RecommendationResult> recommendations,
            ShuffleConfig config,
            Random random
    ) {
        if (recommendations.isEmpty()) {
            return new ArrayList<>();
        }

        List<RecommendationResult> result = new ArrayList<>(recommendations);
        Collections.shuffle(result, random);

        log.debug("Full shuffle: randomly shuffled {} items", recommendations.size());

        return result;
    }
}
