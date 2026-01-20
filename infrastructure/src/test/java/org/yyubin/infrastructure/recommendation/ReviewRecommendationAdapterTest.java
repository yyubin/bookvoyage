package org.yyubin.infrastructure.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.recommendation.port.ReviewRecommendationPort.RecommendationItem;
import org.yyubin.recommendation.service.ReviewRecommendationResult;
import org.yyubin.recommendation.service.ReviewRecommendationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRecommendationAdapter 테스트")
class ReviewRecommendationAdapterTest {

    @Mock
    private ReviewRecommendationService reviewRecommendationService;

    @InjectMocks
    private ReviewRecommendationAdapter adapter;

    @Test
    @DisplayName("추천 결과를 RecommendationItem 리스트로 변환한다")
    void getRecommendations_MapsResultsCorrectly() {
        // Given
        Long userId = 1L;
        Long cursor = null;
        int limit = 10;
        boolean forceRefresh = false;

        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 30);
        List<ReviewRecommendationResult> serviceResults = List.of(
            ReviewRecommendationResult.builder()
                .reviewId(100L)
                .bookId(200L)
                .score(0.95)
                .rank(1)
                .source("COLLABORATIVE")
                .reason("Similar users liked this")
                .createdAt(createdAt)
                .build(),
            ReviewRecommendationResult.builder()
                .reviewId(101L)
                .bookId(201L)
                .score(0.85)
                .rank(2)
                .source("CONTENT_BASED")
                .reason("Based on your reading history")
                .createdAt(createdAt)
                .build()
        );

        when(reviewRecommendationService.recommendFeed(userId, cursor, limit, forceRefresh))
            .thenReturn(serviceResults);

        // When
        List<RecommendationItem> result = adapter.getRecommendations(userId, cursor, limit, forceRefresh);

        // Then
        assertThat(result).hasSize(2);

        RecommendationItem first = result.get(0);
        assertThat(first.reviewId()).isEqualTo(100L);
        assertThat(first.bookId()).isEqualTo(200L);
        assertThat(first.score()).isEqualTo(0.95);
        assertThat(first.rank()).isEqualTo(1);
        assertThat(first.source()).isEqualTo("COLLABORATIVE");
        assertThat(first.reason()).isEqualTo("Similar users liked this");
        assertThat(first.createdAt()).isEqualTo(createdAt);

        RecommendationItem second = result.get(1);
        assertThat(second.reviewId()).isEqualTo(101L);
        assertThat(second.bookId()).isEqualTo(201L);

        verify(reviewRecommendationService).recommendFeed(userId, cursor, limit, forceRefresh);
    }

    @Test
    @DisplayName("빈 추천 결과는 빈 리스트를 반환한다")
    void getRecommendations_EmptyResults_ReturnsEmptyList() {
        // Given
        Long userId = 1L;
        when(reviewRecommendationService.recommendFeed(userId, null, 10, false))
            .thenReturn(List.of());

        // When
        List<RecommendationItem> result = adapter.getRecommendations(userId, null, 10, false);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("forceRefresh 플래그가 서비스에 전달된다")
    void getRecommendations_ForceRefresh_PassedToService() {
        // Given
        Long userId = 1L;
        when(reviewRecommendationService.recommendFeed(userId, 50L, 20, true))
            .thenReturn(List.of());

        // When
        adapter.getRecommendations(userId, 50L, 20, true);

        // Then
        verify(reviewRecommendationService).recommendFeed(userId, 50L, 20, true);
    }
}
