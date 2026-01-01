package org.yyubin.recommendation.sampling.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.WindowSamplingConfig;

import java.util.Map;

/**
 * 셔플 전략 팩토리
 * <p>
 * Enum 타입에 따라 적절한 전략 구현체를 반환
 */
@Component
@RequiredArgsConstructor
public class ShuffleStrategyFactory {

    private final NoneShuffleStrategy noneStrategy;
    private final PartialShuffleStrategy partialStrategy;
    private final WindowShuffleStrategy windowStrategy;
    private final FullShuffleStrategy fullStrategy;

    /**
     * 전략 타입에 따른 구현체 반환
     *
     * @param strategyType 전략 타입
     * @return 전략 구현체
     */
    public ShuffleStrategy getStrategy(WindowSamplingConfig.ShuffleStrategy strategyType) {
        return switch (strategyType) {
            case NONE -> noneStrategy;
            case PARTIAL -> partialStrategy;
            case WINDOW -> windowStrategy;
            case FULL -> fullStrategy;
        };
    }
}
