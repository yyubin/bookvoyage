package org.yyubin.infrastructure.feed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.feed.FeedItemPersistenceAdapter;
import org.yyubin.infrastructure.stream.feed.RedisFeedItemAdapter;
import org.yyubin.recommendation.service.ReviewRecommendationResult;
import org.yyubin.recommendation.service.ReviewRecommendationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FallbackFeedService 테스트")
class FallbackFeedServiceTest {

    @Mock
    private RedisFeedItemAdapter redisAdapter;

    @Mock
    private FeedItemPersistenceAdapter dbAdapter;

    @Mock
    private ReviewRecommendationService reviewRecommendationService;

    @InjectMocks
    private FallbackFeedService fallbackFeedService;

    @Test
    @DisplayName("Redis에 데이터가 있으면 Redis에서 반환한다")
    void loadFeed_RedisHasData_ReturnsFromRedis() {
        // Given
        UserId userId = new UserId(1L);
        List<FeedItem> redisItems = List.of(
            createFeedItem(1L, 100L),
            createFeedItem(2L, 101L)
        );
        when(redisAdapter.loadFeed(userId, null, 10)).thenReturn(redisItems);

        // When
        List<FeedItem> result = fallbackFeedService.loadFeed(userId, null, 10);

        // Then
        assertThat(result).hasSize(2);
        verify(redisAdapter).loadFeed(userId, null, 10);
        verify(reviewRecommendationService, never()).recommendFeed(any(), anyInt(), eq(false));
        verify(dbAdapter, never()).loadFeed(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Redis가 비어있으면 추천으로 워밍업한다")
    void loadFeed_RedisEmpty_WarmsUpFromRecommendations() {
        // Given
        UserId userId = new UserId(1L);
        when(redisAdapter.loadFeed(userId, null, 10)).thenReturn(List.of());

        List<ReviewRecommendationResult> recommendations = List.of(
            createRecommendationResult(100L),
            createRecommendationResult(101L)
        );
        when(reviewRecommendationService.recommendFeed(1L, 10, false)).thenReturn(recommendations);

        // When
        List<FeedItem> result = fallbackFeedService.loadFeed(userId, null, 10);

        // Then
        assertThat(result).hasSize(2);
        verify(reviewRecommendationService).recommendFeed(1L, 10, false);
        verify(redisAdapter, times(2)).save(any(FeedItem.class));
    }

    @Test
    @DisplayName("추천도 비어있으면 DB에서 조회한다")
    void loadFeed_RecommendationsEmpty_FallsBackToDb() {
        // Given
        UserId userId = new UserId(1L);
        when(redisAdapter.loadFeed(userId, null, 10)).thenReturn(List.of());
        when(reviewRecommendationService.recommendFeed(1L, 10, false)).thenReturn(List.of());

        List<FeedItem> dbItems = List.of(createFeedItem(1L, 100L));
        when(dbAdapter.loadFeed(userId, null, 10)).thenReturn(dbItems);

        // When
        List<FeedItem> result = fallbackFeedService.loadFeed(userId, null, 10);

        // Then
        assertThat(result).hasSize(1);
        verify(dbAdapter).loadFeed(userId, null, 10);
    }

    @Test
    @DisplayName("결과가 size보다 크면 잘라서 반환한다")
    void loadFeed_TrimResultsToSize() {
        // Given
        UserId userId = new UserId(1L);
        List<FeedItem> manyItems = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            manyItems.add(createFeedItem((long) i, 100L + i));
        }
        when(redisAdapter.loadFeed(userId, null, 5)).thenReturn(manyItems);

        // When
        List<FeedItem> result = fallbackFeedService.loadFeed(userId, null, 5);

        // Then
        assertThat(result).hasSize(5);
    }

    @Test
    @DisplayName("Redis가 null을 반환하면 추천으로 폴백한다")
    void loadFeed_RedisReturnsNull_FallsBackToRecommendations() {
        // Given
        UserId userId = new UserId(1L);
        when(redisAdapter.loadFeed(userId, null, 10)).thenReturn(null);

        List<ReviewRecommendationResult> recommendations = List.of(
            createRecommendationResult(100L)
        );
        when(reviewRecommendationService.recommendFeed(1L, 10, false)).thenReturn(recommendations);

        // When
        List<FeedItem> result = fallbackFeedService.loadFeed(userId, null, 10);

        // Then
        assertThat(result).hasSize(1);
        verify(reviewRecommendationService).recommendFeed(1L, 10, false);
    }

    @Test
    @DisplayName("추천 결과에 null reviewId가 있으면 제외한다")
    void loadFeed_RecommendationWithNullReviewId_IsExcluded() {
        // Given
        UserId userId = new UserId(1L);
        when(redisAdapter.loadFeed(userId, null, 10)).thenReturn(List.of());

        ReviewRecommendationResult validRec = createRecommendationResult(100L);
        ReviewRecommendationResult nullRec = ReviewRecommendationResult.builder()
            .reviewId(null)
            .createdAt(LocalDateTime.now())
            .build();

        when(reviewRecommendationService.recommendFeed(1L, 10, false))
            .thenReturn(List.of(validRec, nullRec));

        // When
        List<FeedItem> result = fallbackFeedService.loadFeed(userId, null, 10);

        // Then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("cursorScore가 전달된다")
    void loadFeed_CursorScore_IsPassedToRedis() {
        // Given
        UserId userId = new UserId(1L);
        Double cursorScore = 123.45;
        when(redisAdapter.loadFeed(userId, cursorScore, 10)).thenReturn(List.of(createFeedItem(1L, 100L)));

        // When
        fallbackFeedService.loadFeed(userId, cursorScore, 10);

        // Then
        verify(redisAdapter).loadFeed(userId, cursorScore, 10);
    }

    private FeedItem createFeedItem(Long id, Long reviewId) {
        return FeedItem.of(
            id,
            new UserId(1L),
            ReviewId.of(reviewId),
            LocalDateTime.of(2024, 1, 1, 10, 0)
        );
    }

    private ReviewRecommendationResult createRecommendationResult(Long reviewId) {
        return ReviewRecommendationResult.builder()
            .reviewId(reviewId)
            .bookId(200L)
            .score(0.9)
            .rank(1)
            .source("TEST")
            .createdAt(LocalDateTime.now())
            .build();
    }
}
