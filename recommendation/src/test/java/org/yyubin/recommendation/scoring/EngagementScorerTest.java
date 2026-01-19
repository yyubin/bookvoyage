package org.yyubin.recommendation.scoring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.yyubin.recommendation.candidate.RecommendationCandidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EngagementScorer 테스트")
class EngagementScorerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private EngagementScorer engagementScorer;

    private Long userId;
    private RecommendationCandidate candidate;

    @BeforeEach
    void setUp() {
        userId = 1L;
        candidate = RecommendationCandidate.builder()
                .bookId(100L)
                .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                .initialScore(0.8)
                .build();
    }

    @Test
    @DisplayName("세션 부스트가 있으면 정규화된 점수 반환")
    void score_WithSessionBoost_ReturnsNormalizedScore() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:books", "100")).thenReturn("0.3");

        // When
        double score = engagementScorer.score(userId, candidate);

        // Then
        // 0.3 / 0.5 = 0.6
        assertThat(score).isEqualTo(0.6);
    }

    @Test
    @DisplayName("세션 부스트가 0.5 이상이면 1.0 반환")
    void score_WithHighSessionBoost_ReturnsOne() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:books", "100")).thenReturn("0.5");

        // When
        double score = engagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("세션 부스트가 0.5 초과면 1.0 반환 (상한)")
    void score_WithVeryHighSessionBoost_ReturnsOne() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:books", "100")).thenReturn("1.0");

        // When
        double score = engagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("세션 부스트가 없으면 0점 반환")
    void score_WithoutSessionBoost_ReturnsZero() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null);

        // When
        double score = engagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Redis 예외 발생 시 0점 반환")
    void score_RedisException_ReturnsZero() {
        // Given
        when(redisTemplate.opsForHash()).thenThrow(new RuntimeException("Redis error"));

        // When
        double score = engagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("세션 부스트가 0이면 0점 반환")
    void score_WithZeroSessionBoost_ReturnsZero() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:books", "100")).thenReturn("0.0");

        // When
        double score = engagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("다른 사용자의 세션 부스트는 별도 키로 조회")
    void score_DifferentUser_UsesDifferentKey() {
        // Given
        Long differentUserId = 2L;
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:2:books", "100")).thenReturn("0.4");

        // When
        double score = engagementScorer.score(differentUserId, candidate);

        // Then
        assertThat(score).isEqualTo(0.8);
    }

    @Test
    @DisplayName("getName은 EngagementScorer를 반환")
    void getName_ReturnsEngagementScorer() {
        // When
        String name = engagementScorer.getName();

        // Then
        assertThat(name).isEqualTo("EngagementScorer");
    }

    @Test
    @DisplayName("getDefaultWeight는 0.15를 반환")
    void getDefaultWeight_Returns015() {
        // When
        double weight = engagementScorer.getDefaultWeight();

        // Then
        assertThat(weight).isEqualTo(0.15);
    }
}
