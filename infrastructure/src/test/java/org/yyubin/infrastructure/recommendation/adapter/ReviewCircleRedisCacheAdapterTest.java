package org.yyubin.infrastructure.recommendation.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.yyubin.domain.recommendation.ReviewCircle;
import org.yyubin.domain.recommendation.ReviewCircleTopic;
import org.yyubin.domain.recommendation.SimilarUser;
import org.yyubin.domain.recommendation.UserTasteVector;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReviewCircleRedisCacheAdapter 테스트")
class ReviewCircleRedisCacheAdapterTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private ObjectMapper objectMapper;
    private ReviewCircleRedisCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        adapter = new ReviewCircleRedisCacheAdapter(redisTemplate, objectMapper);

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    @DisplayName("TasteVector를 저장한다")
    void saveTasteVector_StoresInRedis() {
        // Given
        UserTasteVector tasteVector = new UserTasteVector(
            1L,
            Map.of("fantasy", 0.8, "romance", 0.3),
            LocalDateTime.of(2024, 1, 1, 10, 0)
        );

        // When
        adapter.saveTasteVector(tasteVector);

        // Then
        verify(valueOperations).set(
            eq("review_circle:taste_vector:1"),
            anyString(),
            eq(7L),
            eq(TimeUnit.DAYS)
        );
    }

    @Test
    @DisplayName("TasteVector를 조회한다")
    void getTasteVector_RetrievesFromRedis() throws Exception {
        // Given
        Long userId = 1L;
        String json = """
            {
                "userId": 1,
                "vector": {"fantasy": 0.8, "romance": 0.3},
                "calculatedAt": "2024-01-01T10:00:00"
            }
            """;
        when(valueOperations.get("review_circle:taste_vector:1")).thenReturn(json);

        // When
        Optional<UserTasteVector> result = adapter.getTasteVector(userId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(1L);
        assertThat(result.get().vector()).containsEntry("fantasy", 0.8);
    }

    @Test
    @DisplayName("존재하지 않는 TasteVector 조회 시 빈 Optional 반환")
    void getTasteVector_NotFound_ReturnsEmpty() {
        // Given
        Long userId = 999L;
        when(valueOperations.get("review_circle:taste_vector:999")).thenReturn(null);

        // When
        Optional<UserTasteVector> result = adapter.getTasteVector(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("SimilarUsers를 저장한다")
    void saveSimilarUsers_StoresInZSet() {
        // Given
        Long userId = 1L;
        List<SimilarUser> similarUsers = List.of(
            SimilarUser.of(2L, 0.95),
            SimilarUser.of(3L, 0.85)
        );

        // When
        adapter.saveSimilarUsers(userId, similarUsers);

        // Then
        verify(redisTemplate).delete("review_circle:similar_users:1");
        verify(zSetOperations).add("review_circle:similar_users:1", "2", 0.95);
        verify(zSetOperations).add("review_circle:similar_users:1", "3", 0.85);
        verify(redisTemplate).expire("review_circle:similar_users:1", 1, TimeUnit.DAYS);
    }

    @Test
    @DisplayName("SimilarUsers를 조회한다")
    void getSimilarUsers_RetrievesFromZSet() {
        // Given
        Long userId = 1L;
        Set<ZSetOperations.TypedTuple<String>> tuples = Set.of(
            createTuple("2", 0.95),
            createTuple("3", 0.85)
        );
        when(zSetOperations.reverseRangeWithScores("review_circle:similar_users:1", 0, -1))
            .thenReturn(tuples);

        // When
        List<SimilarUser> result = adapter.getSimilarUsers(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).anyMatch(u -> u.userId().equals(2L) && u.similarityScore() == 0.95);
        assertThat(result).anyMatch(u -> u.userId().equals(3L) && u.similarityScore() == 0.85);
    }

    @Test
    @DisplayName("SimilarUsers가 없으면 빈 리스트 반환")
    void getSimilarUsers_NotFound_ReturnsEmptyList() {
        // Given
        Long userId = 999L;
        when(zSetOperations.reverseRangeWithScores("review_circle:similar_users:999", 0, -1))
            .thenReturn(null);

        // When
        List<SimilarUser> result = adapter.getSimilarUsers(userId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ReviewCircle을 저장한다")
    void saveReviewCircle_StoresInRedis() {
        // Given
        ReviewCircle reviewCircle = new ReviewCircle(
            1L,
            "weekly",
            List.of(ReviewCircleTopic.of("fantasy", 10, 0.9, LocalDateTime.now())),
            5,
            LocalDateTime.of(2024, 1, 1, 10, 0)
        );

        // When
        adapter.saveReviewCircle(reviewCircle);

        // Then
        verify(valueOperations).set(
            eq("review_circle:topics:1:weekly"),
            anyString(),
            eq(1L),
            eq(TimeUnit.HOURS)
        );
    }

    @Test
    @DisplayName("ReviewCircle을 조회한다")
    void getReviewCircle_RetrievesFromRedis() {
        // Given
        Long userId = 1L;
        String window = "weekly";
        String json = """
            {
                "userId": 1,
                "window": "weekly",
                "topics": [{"keyword": "fantasy", "reviewCount": 10, "score": 0.9, "lastActivityAt": "2024-01-01T10:00:00"}],
                "similarUserCount": 5,
                "calculatedAt": "2024-01-01T10:00:00"
            }
            """;
        when(valueOperations.get("review_circle:topics:1:weekly")).thenReturn(json);

        // When
        Optional<ReviewCircle> result = adapter.getReviewCircle(userId, window);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().userId()).isEqualTo(1L);
        assertThat(result.get().window()).isEqualTo("weekly");
        assertThat(result.get().topics()).hasSize(1);
    }

    @Test
    @DisplayName("TasteVector를 삭제한다")
    void deleteTasteVector_DeletesFromRedis() {
        // Given
        Long userId = 1L;

        // When
        adapter.deleteTasteVector(userId);

        // Then
        verify(redisTemplate).delete("review_circle:taste_vector:1");
    }

    @Test
    @DisplayName("SimilarUsers를 삭제한다")
    void deleteSimilarUsers_DeletesFromRedis() {
        // Given
        Long userId = 1L;

        // When
        adapter.deleteSimilarUsers(userId);

        // Then
        verify(redisTemplate).delete("review_circle:similar_users:1");
    }

    @Test
    @DisplayName("ReviewCircle을 삭제한다")
    void deleteReviewCircle_DeletesFromRedis() {
        // Given
        Long userId = 1L;
        String window = "weekly";

        // When
        adapter.deleteReviewCircle(userId, window);

        // Then
        verify(redisTemplate).delete("review_circle:topics:1:weekly");
    }

    private ZSetOperations.TypedTuple<String> createTuple(String value, double score) {
        return new ZSetOperations.TypedTuple<>() {
            @Override
            public String getValue() {
                return value;
            }

            @Override
            public Double getScore() {
                return score;
            }

            @Override
            public int compareTo(ZSetOperations.TypedTuple<String> o) {
                return Double.compare(score, o.getScore());
            }
        };
    }
}
