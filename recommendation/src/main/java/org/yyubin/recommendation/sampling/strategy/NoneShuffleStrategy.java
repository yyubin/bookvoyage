package org.yyubin.recommendation.sampling.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 셔플 없음 전략
 * - 원본 순서 그대로 유지
 */
@Slf4j
@Component
public class NoneShuffleStrategy implements ShuffleStrategy {

    @Override
    public List<RecommendationResult> shuffle(
            List<RecommendationResult> recommendations,
            ShuffleConfig config,
            Random random
    ) {
        log.debug("None shuffle: maintaining original order for {} items", recommendations.size());
        return new ArrayList<>(recommendations);
    }
}
