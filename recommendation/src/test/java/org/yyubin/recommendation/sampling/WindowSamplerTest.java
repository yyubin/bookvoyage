package org.yyubin.recommendation.sampling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.config.WindowSamplingConfig;
import org.yyubin.recommendation.sampling.strategy.*;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WindowSampler 테스트")
class WindowSamplerTest {

    @Mock
    private WindowSamplingConfig config;

    @Mock
    private ShuffleStrategyFactory strategyFactory;

    @Mock
    private ShuffleStrategy mockStrategy;

    private WindowSampler windowSampler;

    @BeforeEach
    void setUp() {
        windowSampler = new WindowSampler(config, strategyFactory);
    }

    @Test
    @DisplayName("샘플링 비활성화 시 원본 리스트 그대로 반환")
    void applySampling_DisabledSampling_ReturnsOriginalList() {
        // Given
        when(config.isEnabled()).thenReturn(false);
        List<RecommendationResult> recommendations = createRecommendations(10);

        // When
        List<RecommendationResult> result = windowSampler.applySampling(recommendations, "session123");

        // Then
        assertThat(result).isSameAs(recommendations);
        verify(strategyFactory, never()).getStrategy(any());
    }

    @Test
    @DisplayName("빈 리스트 전달 시 빈 리스트 반환")
    void applySampling_EmptyList_ReturnsEmptyList() {
        // Given
        when(config.isEnabled()).thenReturn(true);
        List<RecommendationResult> recommendations = new ArrayList<>();

        // When
        List<RecommendationResult> result = windowSampler.applySampling(recommendations, "session123");

        // Then
        assertThat(result).isEmpty();
        verify(strategyFactory, never()).getStrategy(any());
    }

    @Test
    @DisplayName("각 Tier별로 전략이 적용됨")
    void applySampling_AppliesStrategiesPerTier() {
        // Given
        when(config.isEnabled()).thenReturn(true);

        WindowSamplingConfig.TierConfig tier1 = new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.PARTIAL, 2);
        WindowSamplingConfig.TierConfig tier2 = new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.WINDOW, 3);
        WindowSamplingConfig.TierConfig tier3 = new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.FULL, 0);

        when(config.getTier1()).thenReturn(tier1);
        when(config.getTier2()).thenReturn(tier2);
        when(config.getTier3()).thenReturn(tier3);

        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL)).thenReturn(mockStrategy);
        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.WINDOW)).thenReturn(mockStrategy);
        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.FULL)).thenReturn(mockStrategy);

        when(mockStrategy.shuffle(anyList(), any(), any())).thenAnswer(inv -> new ArrayList<>(inv.getArgument(0)));

        List<RecommendationResult> recommendations = createRecommendations(15);

        // When
        windowSampler.applySampling(recommendations, "session123");

        // Then
        verify(strategyFactory).getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL);
        verify(strategyFactory).getStrategy(WindowSamplingConfig.ShuffleStrategy.WINDOW);
        verify(strategyFactory).getStrategy(WindowSamplingConfig.ShuffleStrategy.FULL);
        verify(mockStrategy, times(3)).shuffle(anyList(), any(), any());
    }

    @Test
    @DisplayName("Tier 크기보다 리스트가 작으면 해당 Tier만 처리")
    void applySampling_ListSmallerThanTierSize_ProcessesOnlyAvailableTiers() {
        // Given
        when(config.isEnabled()).thenReturn(true);

        WindowSamplingConfig.TierConfig tier1 = new WindowSamplingConfig.TierConfig(
                10, WindowSamplingConfig.ShuffleStrategy.PARTIAL, 3);
        WindowSamplingConfig.TierConfig tier2 = new WindowSamplingConfig.TierConfig(
                10, WindowSamplingConfig.ShuffleStrategy.WINDOW, 5);
        WindowSamplingConfig.TierConfig tier3 = new WindowSamplingConfig.TierConfig(
                10, WindowSamplingConfig.ShuffleStrategy.FULL, 0);

        when(config.getTier1()).thenReturn(tier1);
        when(config.getTier2()).thenReturn(tier2);
        when(config.getTier3()).thenReturn(tier3);

        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL)).thenReturn(mockStrategy);
        when(mockStrategy.shuffle(anyList(), any(), any())).thenAnswer(inv -> new ArrayList<>(inv.getArgument(0)));

        List<RecommendationResult> recommendations = createRecommendations(5);

        // When
        List<RecommendationResult> result = windowSampler.applySampling(recommendations, "session123");

        // Then
        assertThat(result).hasSize(5);
        verify(strategyFactory, times(1)).getStrategy(any());
    }

    @Test
    @DisplayName("같은 세션 ID는 같은 시드 생성")
    void applySampling_SameSessionId_ConsistentResult() {
        // Given
        when(config.isEnabled()).thenReturn(true);

        WindowSamplingConfig.TierConfig tier1 = new WindowSamplingConfig.TierConfig(
                10, WindowSamplingConfig.ShuffleStrategy.NONE, 0);
        when(config.getTier1()).thenReturn(tier1);
        when(config.getTier2()).thenReturn(new WindowSamplingConfig.TierConfig(0, WindowSamplingConfig.ShuffleStrategy.NONE, 0));
        when(config.getTier3()).thenReturn(new WindowSamplingConfig.TierConfig(0, WindowSamplingConfig.ShuffleStrategy.NONE, 0));

        NoneShuffleStrategy noneStrategy = new NoneShuffleStrategy();
        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.NONE)).thenReturn(noneStrategy);

        List<RecommendationResult> recommendations = createRecommendations(5);
        String sessionId = "consistent-session-123";

        // When
        List<RecommendationResult> result1 = windowSampler.applySampling(recommendations, sessionId);
        List<RecommendationResult> result2 = windowSampler.applySampling(recommendations, sessionId);

        // Then
        assertThat(result1).hasSize(result2.size());
        for (int i = 0; i < result1.size(); i++) {
            assertThat(result1.get(i).getBookId()).isEqualTo(result2.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("null 세션 ID는 시스템 시간 기반 시드 사용")
    void applySampling_NullSessionId_UsesTimeSeed() {
        // Given
        when(config.isEnabled()).thenReturn(true);

        WindowSamplingConfig.TierConfig tier1 = new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.NONE, 0);
        when(config.getTier1()).thenReturn(tier1);
        when(config.getTier2()).thenReturn(new WindowSamplingConfig.TierConfig(0, WindowSamplingConfig.ShuffleStrategy.NONE, 0));
        when(config.getTier3()).thenReturn(new WindowSamplingConfig.TierConfig(0, WindowSamplingConfig.ShuffleStrategy.NONE, 0));

        NoneShuffleStrategy noneStrategy = new NoneShuffleStrategy();
        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.NONE)).thenReturn(noneStrategy);

        List<RecommendationResult> recommendations = createRecommendations(5);

        // When & Then - null 세션 ID도 정상 처리됨
        List<RecommendationResult> result = windowSampler.applySampling(recommendations, null);
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("빈 세션 ID는 시스템 시간 기반 시드 사용")
    void applySampling_EmptySessionId_UsesTimeSeed() {
        // Given
        when(config.isEnabled()).thenReturn(true);

        WindowSamplingConfig.TierConfig tier1 = new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.NONE, 0);
        when(config.getTier1()).thenReturn(tier1);
        when(config.getTier2()).thenReturn(new WindowSamplingConfig.TierConfig(0, WindowSamplingConfig.ShuffleStrategy.NONE, 0));
        when(config.getTier3()).thenReturn(new WindowSamplingConfig.TierConfig(0, WindowSamplingConfig.ShuffleStrategy.NONE, 0));

        NoneShuffleStrategy noneStrategy = new NoneShuffleStrategy();
        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.NONE)).thenReturn(noneStrategy);

        List<RecommendationResult> recommendations = createRecommendations(5);

        // When & Then - 빈 세션 ID도 정상 처리됨
        List<RecommendationResult> result = windowSampler.applySampling(recommendations, "");
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("전체 플로우 테스트 - 실제 전략 구현체 사용")
    void applySampling_FullFlowWithRealStrategies() {
        // Given
        NoneShuffleStrategy noneStrategy = new NoneShuffleStrategy();
        PartialShuffleStrategy partialStrategy = new PartialShuffleStrategy();
        WindowShuffleStrategy windowStrategy = new WindowShuffleStrategy();
        FullShuffleStrategy fullStrategy = new FullShuffleStrategy();
        ShuffleStrategyFactory realFactory = new ShuffleStrategyFactory(
                noneStrategy, partialStrategy, windowStrategy, fullStrategy);

        WindowSamplingConfig realConfig = new WindowSamplingConfig();
        realConfig.setEnabled(true);
        realConfig.setTier1(new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.PARTIAL, 2));
        realConfig.setTier2(new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.WINDOW, 3));
        realConfig.setTier3(new WindowSamplingConfig.TierConfig(
                5, WindowSamplingConfig.ShuffleStrategy.FULL, 0));

        WindowSampler realSampler = new WindowSampler(realConfig, realFactory);
        List<RecommendationResult> recommendations = createRecommendations(15);

        // When
        List<RecommendationResult> result = realSampler.applySampling(recommendations, "test-session");

        // Then
        assertThat(result).hasSize(15);

        // 모든 원본 요소가 포함됨
        Set<Long> originalIds = recommendations.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> resultIds = result.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(originalIds);
    }

    @Test
    @DisplayName("Tier1만 있는 경우 Tier1만 처리")
    void applySampling_OnlyTier1HasItems_ProcessesOnlyTier1() {
        // Given
        when(config.isEnabled()).thenReturn(true);

        WindowSamplingConfig.TierConfig tier1 = new WindowSamplingConfig.TierConfig(
                100, WindowSamplingConfig.ShuffleStrategy.PARTIAL, 5);
        WindowSamplingConfig.TierConfig tier2 = new WindowSamplingConfig.TierConfig(
                100, WindowSamplingConfig.ShuffleStrategy.WINDOW, 5);
        WindowSamplingConfig.TierConfig tier3 = new WindowSamplingConfig.TierConfig(
                100, WindowSamplingConfig.ShuffleStrategy.FULL, 0);

        when(config.getTier1()).thenReturn(tier1);
        when(config.getTier2()).thenReturn(tier2);
        when(config.getTier3()).thenReturn(tier3);

        PartialShuffleStrategy partialStrategy = new PartialShuffleStrategy();
        when(strategyFactory.getStrategy(WindowSamplingConfig.ShuffleStrategy.PARTIAL)).thenReturn(partialStrategy);

        List<RecommendationResult> recommendations = createRecommendations(10);

        // When
        List<RecommendationResult> result = windowSampler.applySampling(recommendations, "session");

        // Then
        assertThat(result).hasSize(10);
        // Tier1만 처리되어야 함
        verify(strategyFactory, times(1)).getStrategy(any());
    }

    private List<RecommendationResult> createRecommendations(int count) {
        List<RecommendationResult> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(RecommendationResult.builder()
                    .bookId((long) i)
                    .score(1.0 - (i * 0.05))
                    .rank(i)
                    .source("TEST")
                    .reason("Test reason")
                    .build());
        }
        return list;
    }
}
