package org.yyubin.infrastructure.recommendation;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.recommendation.port.BookRecommendationPort.RecommendationItem;
import org.yyubin.recommendation.service.RecommendationResult;
import org.yyubin.recommendation.service.RecommendationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookRecommendationAdapter 테스트")
class BookRecommendationAdapterTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private BookRecommendationAdapter adapter;

    @Test
    @DisplayName("추천 결과를 RecommendationItem 리스트로 변환한다")
    void getRecommendations_MapsResultsCorrectly() {
        // Given
        Long userId = 1L;
        Long cursor = null;
        int limit = 10;
        boolean forceRefresh = false;
        boolean enableSampling = true;
        String sessionId = "session-123";

        List<RecommendationResult> serviceResults = List.of(
            RecommendationResult.builder()
                .bookId(100L)
                .score(0.95)
                .rank(1)
                .source("HYBRID")
                .reason("Top pick for you")
                .build(),
            RecommendationResult.builder()
                .bookId(101L)
                .score(0.88)
                .rank(2)
                .source("POPULARITY")
                .reason("Trending this week")
                .build()
        );

        when(recommendationService.generateRecommendations(userId, cursor, limit, forceRefresh, enableSampling, sessionId))
            .thenReturn(serviceResults);

        // When
        List<RecommendationItem> result = adapter.getRecommendations(userId, cursor, limit, forceRefresh, enableSampling, sessionId);

        // Then
        assertThat(result).hasSize(2);

        RecommendationItem first = result.get(0);
        assertThat(first.bookId()).isEqualTo(100L);
        assertThat(first.score()).isEqualTo(0.95);
        assertThat(first.rank()).isEqualTo(1);
        assertThat(first.source()).isEqualTo("HYBRID");
        assertThat(first.reason()).isEqualTo("Top pick for you");

        RecommendationItem second = result.get(1);
        assertThat(second.bookId()).isEqualTo(101L);
        assertThat(second.score()).isEqualTo(0.88);
        assertThat(second.rank()).isEqualTo(2);

        verify(recommendationService).generateRecommendations(userId, cursor, limit, forceRefresh, enableSampling, sessionId);
    }

    @Test
    @DisplayName("빈 추천 결과는 빈 리스트를 반환한다")
    void getRecommendations_EmptyResults_ReturnsEmptyList() {
        // Given
        Long userId = 1L;
        when(recommendationService.generateRecommendations(userId, null, 10, false, false, null))
            .thenReturn(List.of());

        // When
        List<RecommendationItem> result = adapter.getRecommendations(userId, null, 10, false, false, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("모든 파라미터가 서비스에 전달된다")
    void getRecommendations_AllParametersPassed() {
        // Given
        Long userId = 5L;
        Long cursor = 100L;
        int limit = 20;
        boolean forceRefresh = true;
        boolean enableSampling = true;
        String sessionId = "test-session";

        when(recommendationService.generateRecommendations(userId, cursor, limit, forceRefresh, enableSampling, sessionId))
            .thenReturn(List.of());

        // When
        adapter.getRecommendations(userId, cursor, limit, forceRefresh, enableSampling, sessionId);

        // Then
        verify(recommendationService).generateRecommendations(userId, cursor, limit, forceRefresh, enableSampling, sessionId);
    }

    @Test
    @DisplayName("sessionId가 null이어도 정상 동작한다")
    void getRecommendations_NullSessionId_Works() {
        // Given
        Long userId = 1L;
        when(recommendationService.generateRecommendations(userId, null, 10, false, false, null))
            .thenReturn(List.of(
                RecommendationResult.builder()
                    .bookId(1L)
                    .score(0.9)
                    .rank(1)
                    .source("DEFAULT")
                    .reason("Default recommendation")
                    .build()
            ));

        // When
        List<RecommendationItem> result = adapter.getRecommendations(userId, null, 10, false, false, null);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).bookId()).isEqualTo(1L);
    }
}
