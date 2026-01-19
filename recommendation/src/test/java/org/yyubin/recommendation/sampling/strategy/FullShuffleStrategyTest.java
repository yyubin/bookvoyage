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
@DisplayName("FullShuffleStrategy 테스트")
class FullShuffleStrategyTest {

    @InjectMocks
    private FullShuffleStrategy fullShuffleStrategy;

    private Random random;
    private ShuffleStrategy.ShuffleConfig config;

    @BeforeEach
    void setUp() {
        random = new Random(12345L);
        config = new ShuffleStrategy.ShuffleConfig(0, 0);
    }

    @Test
    @DisplayName("전체 리스트가 셔플됨")
    void shuffle_ShufflesEntireList() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(20);

        // When
        List<RecommendationResult> result = fullShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(20);

        // 같은 요소들로 구성됨
        Set<Long> originalIds = recommendations.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> resultIds = result.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(originalIds);
    }

    @Test
    @DisplayName("빈 리스트를 전달하면 빈 리스트 반환")
    void shuffle_EmptyList_ReturnsEmptyList() {
        // Given
        List<RecommendationResult> recommendations = new ArrayList<>();

        // When
        List<RecommendationResult> result = fullShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("새로운 리스트가 반환됨 (원본 불변)")
    void shuffle_ReturnsNewList() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(5);
        List<Long> originalOrder = recommendations.stream()
                .map(RecommendationResult::getBookId).toList();

        // When
        List<RecommendationResult> result = fullShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).isNotSameAs(recommendations);
        // 원본 순서 유지 확인
        for (int i = 0; i < recommendations.size(); i++) {
            assertThat(recommendations.get(i).getBookId()).isEqualTo(originalOrder.get(i));
        }
    }

    @Test
    @DisplayName("단일 항목 리스트도 정상 처리")
    void shuffle_SingleItem_WorksCorrectly() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(1);

        // When
        List<RecommendationResult> result = fullShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("같은 시드로 같은 결과 반환")
    void shuffle_SameSeed_SameResult() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(10);
        Random random1 = new Random(12345L);
        Random random2 = new Random(12345L);

        // When
        List<RecommendationResult> result1 = fullShuffleStrategy.shuffle(recommendations, config, random1);
        List<RecommendationResult> result2 = fullShuffleStrategy.shuffle(recommendations, config, random2);

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
        List<RecommendationResult> recommendations = createRecommendations(20);
        Random random1 = new Random(12345L);
        Random random2 = new Random(99999L);

        // When
        List<RecommendationResult> result1 = fullShuffleStrategy.shuffle(recommendations, config, random1);
        List<RecommendationResult> result2 = fullShuffleStrategy.shuffle(recommendations, config, random2);

        // Then - 충분히 큰 리스트에서 다른 시드면 순서가 다름
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
    @DisplayName("config 값은 무시됨")
    void shuffle_IgnoresConfig() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(10);
        ShuffleStrategy.ShuffleConfig config1 = new ShuffleStrategy.ShuffleConfig(0, 0);
        ShuffleStrategy.ShuffleConfig config2 = new ShuffleStrategy.ShuffleConfig(100, 200);
        Random random1 = new Random(12345L);
        Random random2 = new Random(12345L);

        // When
        List<RecommendationResult> result1 = fullShuffleStrategy.shuffle(recommendations, config1, random1);
        List<RecommendationResult> result2 = fullShuffleStrategy.shuffle(recommendations, config2, random2);

        // Then - 같은 시드면 config가 달라도 같은 결과
        for (int i = 0; i < result1.size(); i++) {
            assertThat(result1.get(i).getBookId()).isEqualTo(result2.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("셔플 후에도 모든 요소가 유지됨")
    void shuffle_AllElementsPreserved() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(100);

        // When
        List<RecommendationResult> result = fullShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(100);
        Set<Long> originalIds = recommendations.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> resultIds = result.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(originalIds);
    }

    private List<RecommendationResult> createRecommendations(int count) {
        List<RecommendationResult> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(RecommendationResult.builder()
                    .bookId((long) i)
                    .score(1.0 - (i * 0.01))
                    .rank(i)
                    .source("TEST")
                    .build());
        }
        return list;
    }
}
