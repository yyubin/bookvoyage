package org.yyubin.recommendation.sampling.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.service.RecommendationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("WindowShuffleStrategy 테스트")
class WindowShuffleStrategyTest {

    @InjectMocks
    private WindowShuffleStrategy windowShuffleStrategy;

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(12345L);
    }

    @Test
    @DisplayName("윈도우 크기만큼 나누어 각각 셔플됨")
    void shuffle_ShufflesWithinWindows() {
        // Given
        int windowSize = 4;
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, windowSize);
        List<RecommendationResult> recommendations = createRecommendations(12);

        // When
        List<RecommendationResult> result = windowShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(12);

        // 각 윈도우 내의 요소들이 같은 요소들로 구성됨 확인
        for (int windowStart = 0; windowStart < 12; windowStart += windowSize) {
            int windowEnd = Math.min(windowStart + windowSize, 12);
            Set<Long> originalWindowIds = recommendations.subList(windowStart, windowEnd)
                    .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
            Set<Long> resultWindowIds = result.subList(windowStart, windowEnd)
                    .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
            assertThat(resultWindowIds).containsExactlyInAnyOrderElementsOf(originalWindowIds);
        }
    }

    @Test
    @DisplayName("빈 리스트를 전달하면 빈 리스트 반환")
    void shuffle_EmptyList_ReturnsEmptyList() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, 5);
        List<RecommendationResult> recommendations = new ArrayList<>();

        // When
        List<RecommendationResult> result = windowShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("windowSize가 0이면 기본값(8) 사용")
    void shuffle_ZeroWindowSize_UsesDefault() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, 0);
        List<RecommendationResult> recommendations = createRecommendations(16);

        // When
        List<RecommendationResult> result = windowShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(16);

        // 기본 윈도우 크기 8로 첫 번째와 두 번째 윈도우 확인
        Set<Long> firstWindowOriginal = recommendations.subList(0, 8)
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> firstWindowResult = result.subList(0, 8)
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(firstWindowResult).containsExactlyInAnyOrderElementsOf(firstWindowOriginal);

        Set<Long> secondWindowOriginal = recommendations.subList(8, 16)
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> secondWindowResult = result.subList(8, 16)
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(secondWindowResult).containsExactlyInAnyOrderElementsOf(secondWindowOriginal);
    }

    @Test
    @DisplayName("리스트 크기가 윈도우 크기로 나누어 떨어지지 않아도 정상 처리")
    void shuffle_ListSizeNotMultipleOfWindowSize_WorksCorrectly() {
        // Given
        int windowSize = 4;
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, windowSize);
        List<RecommendationResult> recommendations = createRecommendations(10); // 4 + 4 + 2

        // When
        List<RecommendationResult> result = windowShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(10);

        // 마지막 불완전한 윈도우도 올바르게 처리됨
        Set<Long> lastWindowOriginal = recommendations.subList(8, 10)
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> lastWindowResult = result.subList(8, 10)
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(lastWindowResult).containsExactlyInAnyOrderElementsOf(lastWindowOriginal);
    }

    @Test
    @DisplayName("리스트 크기가 윈도우 크기보다 작으면 단일 윈도우로 처리")
    void shuffle_ListSmallerThanWindowSize_SingleWindow() {
        // Given
        int windowSize = 10;
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, windowSize);
        List<RecommendationResult> recommendations = createRecommendations(5);

        // When
        List<RecommendationResult> result = windowShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(5);

        Set<Long> originalIds = recommendations.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> resultIds = result.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(originalIds);
    }

    @Test
    @DisplayName("같은 시드로 같은 결과 반환")
    void shuffle_SameSeed_SameResult() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, 4);
        List<RecommendationResult> recommendations = createRecommendations(12);
        Random random1 = new Random(12345L);
        Random random2 = new Random(12345L);

        // When
        List<RecommendationResult> result1 = windowShuffleStrategy.shuffle(recommendations, config, random1);
        List<RecommendationResult> result2 = windowShuffleStrategy.shuffle(recommendations, config, random2);

        // Then
        assertThat(result1).hasSize(result2.size());
        for (int i = 0; i < result1.size(); i++) {
            assertThat(result1.get(i).getBookId()).isEqualTo(result2.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("다른 시드로 다른 결과 반환")
    void shuffle_DifferentSeed_DifferentResult() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, 4);
        List<RecommendationResult> recommendations = createRecommendations(20);
        Random random1 = new Random(12345L);
        Random random2 = new Random(99999L);

        // When
        List<RecommendationResult> result1 = windowShuffleStrategy.shuffle(recommendations, config, random1);
        List<RecommendationResult> result2 = windowShuffleStrategy.shuffle(recommendations, config, random2);

        // Then - 최소 하나의 윈도우에서 순서가 다름
        boolean hasDifference = false;
        for (int i = 0; i < result1.size(); i++) {
            if (!result1.get(i).getBookId().equals(result2.get(i).getBookId())) {
                hasDifference = true;
                break;
            }
        }
        assertThat(hasDifference).isTrue();
    }

    @Test
    @DisplayName("단일 항목 리스트도 정상 처리")
    void shuffle_SingleItem_WorksCorrectly() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0, 5);
        List<RecommendationResult> recommendations = createRecommendations(1);

        // When
        List<RecommendationResult> result = windowShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
    }

    private List<RecommendationResult> createRecommendations(int count) {
        List<RecommendationResult> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(RecommendationResult.builder()
                    .bookId((long) i)
                    .score(1.0 - (i * 0.05))
                    .rank(i)
                    .source("TEST")
                    .build());
        }
        return list;
    }
}
