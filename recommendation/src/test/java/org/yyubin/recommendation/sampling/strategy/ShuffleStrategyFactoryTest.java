package org.yyubin.recommendation.sampling.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.config.WindowSamplingConfig;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShuffleStrategyFactory 테스트")
class ShuffleStrategyFactoryTest {

    @Mock
    private NoneShuffleStrategy noneStrategy;

    @Mock
    private PartialShuffleStrategy partialStrategy;

    @Mock
    private WindowShuffleStrategy windowStrategy;

    @Mock
    private FullShuffleStrategy fullStrategy;

    private ShuffleStrategyFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ShuffleStrategyFactory(noneStrategy, partialStrategy, windowStrategy, fullStrategy);
    }

    @Test
    @DisplayName("NONE 전략 타입으로 NoneShuffleStrategy 반환")
    void getStrategy_None_ReturnsNoneStrategy() {
        // When
        ShuffleStrategy result = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.NONE);

        // Then
        assertThat(result).isSameAs(noneStrategy);
    }

    @Test
    @DisplayName("PARTIAL 전략 타입으로 PartialShuffleStrategy 반환")
    void getStrategy_Partial_ReturnsPartialStrategy() {
        // When
        ShuffleStrategy result = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL);

        // Then
        assertThat(result).isSameAs(partialStrategy);
    }

    @Test
    @DisplayName("WINDOW 전략 타입으로 WindowShuffleStrategy 반환")
    void getStrategy_Window_ReturnsWindowStrategy() {
        // When
        ShuffleStrategy result = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.WINDOW);

        // Then
        assertThat(result).isSameAs(windowStrategy);
    }

    @Test
    @DisplayName("FULL 전략 타입으로 FullShuffleStrategy 반환")
    void getStrategy_Full_ReturnsFullStrategy() {
        // When
        ShuffleStrategy result = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.FULL);

        // Then
        assertThat(result).isSameAs(fullStrategy);
    }

    @Test
    @DisplayName("같은 전략 타입으로 항상 같은 인스턴스 반환")
    void getStrategy_SameType_ReturnsSameInstance() {
        // When
        ShuffleStrategy result1 = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL);
        ShuffleStrategy result2 = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL);

        // Then
        assertThat(result1).isSameAs(result2);
    }

    @Test
    @DisplayName("다른 전략 타입은 다른 인스턴스 반환")
    void getStrategy_DifferentTypes_ReturnsDifferentInstances() {
        // When
        ShuffleStrategy noneResult = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.NONE);
        ShuffleStrategy partialResult = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL);
        ShuffleStrategy windowResult = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.WINDOW);
        ShuffleStrategy fullResult = factory.getStrategy(WindowSamplingConfig.ShuffleStrategy.FULL);

        // Then
        assertThat(noneResult).isNotSameAs(partialResult);
        assertThat(partialResult).isNotSameAs(windowResult);
        assertThat(windowResult).isNotSameAs(fullResult);
        assertThat(fullResult).isNotSameAs(noneResult);
    }
}
