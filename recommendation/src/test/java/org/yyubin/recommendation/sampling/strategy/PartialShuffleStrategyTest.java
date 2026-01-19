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
@DisplayName("PartialShuffleStrategy 테스트")
class PartialShuffleStrategyTest {

    @InjectMocks
    private PartialShuffleStrategy partialShuffleStrategy;

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(12345L);
    }

    @Test
    @DisplayName("상위 N개는 고정되고 나머지는 셔플됨")
    void shuffle_FixesTopNAndShufflesRest() {
        // Given
        int fixedTopN = 3;
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(fixedTopN);
        List<RecommendationResult> recommendations = createRecommendations(10);

        // When
        List<RecommendationResult> result = partialShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(10);

        // 상위 3개는 순서 유지
        for (int i = 0; i < fixedTopN; i++) {
            assertThat(result.get(i).getBookId()).isEqualTo(recommendations.get(i).getBookId());
        }

        // 나머지 항목은 동일한 요소들로 구성 (순서만 다름)
        Set<Long> originalRemainingIds = recommendations.subList(fixedTopN, recommendations.size())
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> resultRemainingIds = result.subList(fixedTopN, result.size())
                .stream().map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(resultRemainingIds).containsExactlyInAnyOrderElementsOf(originalRemainingIds);
    }

    @Test
    @DisplayName("빈 리스트를 전달하면 빈 리스트 반환")
    void shuffle_EmptyList_ReturnsEmptyList() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(3);
        List<RecommendationResult> recommendations = new ArrayList<>();

        // When
        List<RecommendationResult> result = partialShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("fixedTopN이 리스트 크기보다 크면 전체 고정")
    void shuffle_FixedTopNLargerThanList_FixesAll() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(10);
        List<RecommendationResult> recommendations = createRecommendations(5);

        // When
        List<RecommendationResult> result = partialShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(5);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getBookId()).isEqualTo(recommendations.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("fixedTopN이 0이면 전체 셔플")
    void shuffle_FixedTopNZero_ShufflesAll() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(0);
        List<RecommendationResult> recommendations = createRecommendations(5);

        // When
        List<RecommendationResult> result = partialShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(5);

        // 모든 요소가 포함됨
        Set<Long> originalIds = recommendations.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        Set<Long> resultIds = result.stream()
                .map(RecommendationResult::getBookId).collect(Collectors.toSet());
        assertThat(resultIds).containsExactlyInAnyOrderElementsOf(originalIds);
    }

    @Test
    @DisplayName("fixedTopN과 리스트 크기가 같으면 셔플 없음")
    void shuffle_FixedTopNEqualsListSize_NoShuffle() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(5);
        List<RecommendationResult> recommendations = createRecommendations(5);

        // When
        List<RecommendationResult> result = partialShuffleStrategy.shuffle(recommendations, config, random);

        // Then
        assertThat(result).hasSize(5);
        for (int i = 0; i < result.size(); i++) {
            assertThat(result.get(i).getBookId()).isEqualTo(recommendations.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("같은 시드로 같은 결과 반환")
    void shuffle_SameSeed_SameResult() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(2);
        List<RecommendationResult> recommendations = createRecommendations(10);
        Random random1 = new Random(12345L);
        Random random2 = new Random(12345L);

        // When
        List<RecommendationResult> result1 = partialShuffleStrategy.shuffle(recommendations, config, random1);
        List<RecommendationResult> result2 = partialShuffleStrategy.shuffle(recommendations, config, random2);

        // Then
        assertThat(result1).hasSize(result2.size());
        for (int i = 0; i < result1.size(); i++) {
            assertThat(result1.get(i).getBookId()).isEqualTo(result2.get(i).getBookId());
        }
    }

    @Test
    @DisplayName("다른 시드로 다른 결과 반환 (충분히 큰 리스트)")
    void shuffle_DifferentSeed_DifferentResult() {
        // Given
        ShuffleStrategy.ShuffleConfig config = new ShuffleStrategy.ShuffleConfig(2);
        List<RecommendationResult> recommendations = createRecommendations(20);
        Random random1 = new Random(12345L);
        Random random2 = new Random(99999L);

        // When
        List<RecommendationResult> result1 = partialShuffleStrategy.shuffle(recommendations, config, random1);
        List<RecommendationResult> result2 = partialShuffleStrategy.shuffle(recommendations, config, random2);

        // Then
        // 상위 2개는 같아야 함
        assertThat(result1.get(0).getBookId()).isEqualTo(result2.get(0).getBookId());
        assertThat(result1.get(1).getBookId()).isEqualTo(result2.get(1).getBookId());

        // 나머지는 높은 확률로 다름 (18개 중 하나라도 다르면 성공)
        boolean hasDifference = false;
        for (int i = 2; i < result1.size(); i++) {
            if (!result1.get(i).getBookId().equals(result2.get(i).getBookId())) {
                hasDifference = true;
                break;
            }
        }
        assertThat(hasDifference).isTrue();
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
