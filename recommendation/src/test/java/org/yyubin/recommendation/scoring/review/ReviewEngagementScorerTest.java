package org.yyubin.recommendation.scoring.review;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewEngagementScorer 테스트")
class ReviewEngagementScorerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private ReviewEngagementScorer reviewEngagementScorer;

    private Long userId;
    private ReviewRecommendationCandidate candidate;

    @BeforeEach
    void setUp() {
        userId = 1L;
        candidate = ReviewRecommendationCandidate.builder()
                .reviewId(100L)
                .bookId(10L)
                .build();
    }

    @Test
    @DisplayName("userId가 null이면 0 반환")
    void score_NullUserId_ReturnsZero() {
        // Given
        // When
        double score = reviewEngagementScorer.score(null, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("reviewId가 null이면 0 반환")
    void score_NullReviewId_ReturnsZero() {
        // Given
        ReviewRecommendationCandidate candidateWithNullReviewId = ReviewRecommendationCandidate.builder()
                .reviewId(null)
                .bookId(10L)
                .build();

        // When
        double score = reviewEngagementScorer.score(userId, candidateWithNullReviewId);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("세션 부스트가 있으면 정규화된 점수 반환")
    void score_WithSessionBoost_ReturnsNormalizedScore() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:reviews", "100")).thenReturn("0.3");

        // When
        double score = reviewEngagementScorer.score(userId, candidate);

        // Then
        // 0.3 / 0.5 = 0.6
        assertThat(score).isEqualTo(0.6);
    }

    @Test
    @DisplayName("세션 부스트가 0.5 이상이면 1.0 반환")
    void score_WithHighSessionBoost_ReturnsOne() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:reviews", "100")).thenReturn("0.5");

        // When
        double score = reviewEngagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("세션 부스트가 0.5 초과면 1.0 반환 (상한)")
    void score_WithVeryHighSessionBoost_ReturnsOne() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:reviews", "100")).thenReturn("1.0");

        // When
        double score = reviewEngagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("세션 부스트가 없으면 0 반환")
    void score_WithoutSessionBoost_ReturnsZero() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get(anyString(), anyString())).thenReturn(null);

        // When
        double score = reviewEngagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Redis 예외 발생 시 0 반환")
    void score_RedisException_ReturnsZero() {
        // Given
        when(redisTemplate.opsForHash()).thenThrow(new RuntimeException("Redis error"));

        // When
        double score = reviewEngagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("세션 부스트가 0이면 0 반환")
    void score_WithZeroSessionBoost_ReturnsZero() {
        // Given
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:reviews", "100")).thenReturn("0.0");

        // When
        double score = reviewEngagementScorer.score(userId, candidate);

        // Then
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("다른 사용자의 세션 부스트는 별도 키로 조회")
    void score_DifferentUser_UsesDifferentKey() {
        // Given
        Long differentUserId = 2L;
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:2:reviews", "100")).thenReturn("0.4");

        // When
        double score = reviewEngagementScorer.score(differentUserId, candidate);

        // Then
        // 0.4 / 0.5 = 0.8
        assertThat(score).isEqualTo(0.8);
    }

    @Test
    @DisplayName("다른 리뷰 ID는 별도 필드로 조회")
    void score_DifferentReviewId_UsesDifferentField() {
        // Given
        ReviewRecommendationCandidate differentCandidate = ReviewRecommendationCandidate.builder()
                .reviewId(200L)
                .bookId(10L)
                .build();
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("session:user:1:reviews", "200")).thenReturn("0.25");

        // When
        double score = reviewEngagementScorer.score(userId, differentCandidate);

        // Then
        // 0.25 / 0.5 = 0.5
        assertThat(score).isEqualTo(0.5);
    }
}
