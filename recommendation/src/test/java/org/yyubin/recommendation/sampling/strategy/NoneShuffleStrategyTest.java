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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoneShuffleStrategy 테스트")
class NoneShuffleStrategyTest {

    @InjectMocks
    private NoneShuffleStrategy noneShuffleStrategy;

    private Random random;
    private ShuffleStrategy.ShuffleConfig config;

    @BeforeEach
    void setUp() {
        random = new Random(12345L);
        config = new ShuffleStrategy.ShuffleConfig(3, 5);
    }

    @Test
    @DisplayName("원본 리스트의 순서가 그대로 유지됨")
    void shuffle_MaintainsOriginalOrder() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(5);

        // When
        List<RecommendationResult> result = noneShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(5);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getBookId()).isEqualTo(recommendations.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("빈 리스트를 전달하면 빈 리스트 반환")
    void shuffle_EmptyList_ReturnsEmptyList() {
        // Given
        List<RecommendationResult> recommendations = new ArrayList<>();

        // When
        List<RecommendationResult> result = noneShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("새로운 리스트가 반환됨 (원본 불변)")
    void shuffle_ReturnsNewList() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(3);

        // When
        List<RecommendationResult> result = noneShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).isNotSameAs(recommendations);
    }

    @Test
    @DisplayName("단일 항목 리스트도 정상 처리")
    void shuffle_SingleItem_WorksCorrectly() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(1);

        // When
        List<RecommendationResult> result = noneShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBookId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Random 객체와 config 값이 무시됨")
    void shuffle_IgnoresRandomAndConfig() {
        // Given
        List<RecommendationResult> recommendations = createRecommendations(5);
        Random differentRandom = new Random(99999L);
        ShuffleStrategy.ShuffleConfig differentConfig = new ShuffleStrategy.ShuffleConfig(100, 200);

        // When
        List<RecommendationResult> result1 = noneShuffleStrategy.shuffle(recommendations, config, random);
        List<RecommendationResult> result2 = noneShuffleStrategy.shuffle(recommendations, differentConfig, differentRandom);

        // Then
        assertThat(result1).hasSize(result2.size());
        for (int i = 0; i < result1.size(); i++) {
            assertThat(result1.get(i).getBookId()).isEqualTo(result2.get(i).getBookId());
        }
    }

    private List<RecommendationResult> createRecommendations(int count) {
        List<RecommendationResult> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(RecommendationResult.builder()
                    .bookId((long) i)
                    .score(1.0 - (i * 0.1))
                    .rank(i)
                    .source("TEST")
                    .build());
        }
        return list;
    }
}
