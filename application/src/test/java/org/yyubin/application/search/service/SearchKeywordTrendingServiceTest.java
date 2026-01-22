package org.yyubin.application.search.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.yyubin.application.recommendation.usecase.AnalyzeCommunityTrendUseCase;
import org.yyubin.application.search.port.SearchKeywordTrendingPort;
import org.yyubin.application.search.port.SearchKeywordTrendingPort.TimeWindow;
import org.yyubin.application.search.port.SearchQueryQueuePort;
import org.yyubin.domain.recommendation.CommunityTrend;
import org.yyubin.domain.search.SearchQuery;
import org.yyubin.domain.search.TrendingKeyword;
import org.yyubin.domain.search.TrendingKeyword.TrendDirection;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchKeywordTrendingService 테스트")
class SearchKeywordTrendingServiceTest {

    @Mock
    private SearchKeywordTrendingPort trendingPort;

    @Mock
    private SearchQueryQueuePort queuePort;

    @Mock
    private ObjectProvider<AnalyzeCommunityTrendUseCase> communityTrendUseCaseProvider;

    @Mock
    private AnalyzeCommunityTrendUseCase communityTrendUseCase;

    @InjectMocks
    private SearchKeywordTrendingService searchKeywordTrendingService;

    private SearchQuery testSearchQuery;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        testSearchQuery = new SearchQuery(
                1L,
                100L,
                "session-123",
                "해리포터",
                "해리포터",
                10,
                null,
                null,
                "web",
                now
        );
    }

    @Nested
    @DisplayName("logSearchQuery 메서드")
    class LogSearchQueryMethod {

        @Test
        @DisplayName("검색 쿼리를 큐에 추가하고 트렌딩 점수 증가")
        void logSearchQuery_EnqueuesAndUpdatesTrending() {
            // Given
            SearchQuery query = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", 5, null, null, "web", now
            );

            // When
            searchKeywordTrendingService.logSearchQuery(query);

            // Then
            verify(queuePort).enqueue(query);
            verify(trendingPort).incrementKeywordScore(
                    eq("test"),
                    anyDouble(),
                    eq(Duration.ofDays(1))
            );
        }

        @Test
        @DisplayName("결과가 있는 검색 쿼리는 더 높은 점수")
        void logSearchQuery_HigherScoreForResultsWithContent() {
            // Given
            SearchQuery queryWithResults = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", 10, null, null, "web", now
            );
            SearchQuery queryWithoutResults = new SearchQuery(
                    2L, 100L, "session-123", "test2", "test2", 0, null, null, "web", now
            );

            ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

            // When
            searchKeywordTrendingService.logSearchQuery(queryWithResults);
            searchKeywordTrendingService.logSearchQuery(queryWithoutResults);

            // Then
            verify(trendingPort).incrementKeywordScore(eq("test"), scoreCaptor.capture(), any());
            verify(trendingPort).incrementKeywordScore(eq("test2"), scoreCaptor.capture(), any());

            List<Double> scores = scoreCaptor.getAllValues();
            assertThat(scores.get(0)).isGreaterThan(scores.get(1));
        }

        @Test
        @DisplayName("결과가 null인 경우 기본 점수 사용")
        void logSearchQuery_HandlesNullResultCount() {
            // Given
            SearchQuery queryWithNullResults = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", null, null, null, "web", now
            );

            // When
            searchKeywordTrendingService.logSearchQuery(queryWithNullResults);

            // Then
            verify(queuePort).enqueue(queryWithNullResults);
            verify(trendingPort).incrementKeywordScore(eq("test"), anyDouble(), any());
        }

        @Test
        @DisplayName("최근 검색일수록 더 높은 점수 (1시간 이내)")
        void logSearchQuery_RecentSearchGetsHigherScore() {
            // Given - 최근 검색 (1시간 이내면 1.5x 가중치)
            SearchQuery recentQuery = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", 5, null, null, "web", now
            );

            ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

            // When
            searchKeywordTrendingService.logSearchQuery(recentQuery);

            // Then
            verify(trendingPort).incrementKeywordScore(eq("test"), scoreCaptor.capture(), any());
            // 기본 점수 1.0 * 시간 가중치 1.5 * 결과 가중치 1.2 = 1.8
            assertThat(scoreCaptor.getValue()).isCloseTo(1.8, within(0.0001));
        }
    }

    @Nested
    @DisplayName("getTrendingKeywords 메서드")
    class GetTrendingKeywordsMethod {

        @Test
        @DisplayName("트렌딩 키워드 목록 반환")
        void getTrendingKeywords_ReturnsList() {
            // Given
            int limit = 10;
            List<TrendingKeyword> expectedKeywords = List.of(
                    new TrendingKeyword("해리포터", 100L, 1, TrendDirection.UP),
                    new TrendingKeyword("클린코드", 80L, 2, TrendDirection.STABLE)
            );
            when(trendingPort.getTopKeywords(limit)).thenReturn(expectedKeywords);

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywords(limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).keyword()).isEqualTo("해리포터");
            assertThat(result.get(1).keyword()).isEqualTo("클린코드");
            verify(trendingPort).getTopKeywords(limit);
        }

        @Test
        @DisplayName("빈 목록 반환")
        void getTrendingKeywords_ReturnsEmptyList() {
            // Given
            when(trendingPort.getTopKeywords(anyInt())).thenReturn(List.of());

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywords(10);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTrendingKeywordsByWindow 메서드")
    class GetTrendingKeywordsByWindowMethod {

        @Test
        @DisplayName("시간 윈도우별 트렌딩 키워드 반환")
        void getTrendingKeywordsByWindow_ReturnsList() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 5;
            List<TrendingKeyword> expectedKeywords = List.of(
                    new TrendingKeyword("실시간검색어", 50L, 1, TrendDirection.NEW)
            );
            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(expectedKeywords);

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).keyword()).isEqualTo("실시간검색어");
            verify(trendingPort).getTopKeywordsByWindow(window, limit);
        }

        @Test
        @DisplayName("DAILY 윈도우 트렌딩 키워드 조회")
        void getTrendingKeywordsByWindow_DailyWindow() {
            // Given
            TimeWindow window = TimeWindow.DAILY;
            int limit = 10;
            List<TrendingKeyword> expectedKeywords = List.of(
                    new TrendingKeyword("오늘의책", 200L, 1, TrendDirection.UP)
            );
            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(expectedKeywords);

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).hasSize(1);
            verify(trendingPort).getTopKeywordsByWindow(TimeWindow.DAILY, limit);
        }

        @Test
        @DisplayName("WEEKLY 윈도우 트렌딩 키워드 조회")
        void getTrendingKeywordsByWindow_WeeklyWindow() {
            // Given
            TimeWindow window = TimeWindow.WEEKLY;
            int limit = 10;
            List<TrendingKeyword> expectedKeywords = List.of(
                    new TrendingKeyword("주간베스트", 500L, 1, TrendDirection.STABLE)
            );
            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(expectedKeywords);

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).hasSize(1);
            verify(trendingPort).getTopKeywordsByWindow(TimeWindow.WEEKLY, limit);
        }

        @Test
        @DisplayName("빈 결과시 커뮤니티 트렌드에서 폴백")
        void getTrendingKeywordsByWindow_FallbackToCommunityTrend() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 5;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);

            CommunityTrend communityTrend = CommunityTrend.of(
                    List.of("힐링", "자기계발", "소설"),
                    "힐링 도서가 인기입니다",
                    List.of()
            );
            when(communityTrendUseCase.findCachedTrend()).thenReturn(Optional.of(communityTrend));

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).keyword()).isEqualTo("힐링");
            assertThat(result.get(0).rank()).isEqualTo(1);
            assertThat(result.get(0).trend()).isEqualTo(TrendDirection.NEW);
            assertThat(result.get(1).keyword()).isEqualTo("자기계발");
            assertThat(result.get(2).keyword()).isEqualTo("소설");
        }

        @Test
        @DisplayName("커뮤니티 트렌드 UseCase가 없으면 빈 목록 반환")
        void getTrendingKeywordsByWindow_ReturnsEmptyWhenNoUseCase() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 5;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(null);

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("커뮤니티 트렌드 캐시가 비어있으면 빈 목록 반환")
        void getTrendingKeywordsByWindow_ReturnsEmptyWhenCacheEmpty() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 5;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);
            when(communityTrendUseCase.findCachedTrend()).thenReturn(Optional.empty());

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("커뮤니티 트렌드 조회 중 예외 발생 시 빈 목록 반환")
        void getTrendingKeywordsByWindow_ReturnsEmptyOnException() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 5;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);
            when(communityTrendUseCase.findCachedTrend()).thenThrow(new RuntimeException("Cache error"));

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("폴백 시 limit보다 적은 키워드 반환")
        void getTrendingKeywordsByWindow_FallbackRespectsLimit() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 2;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);

            CommunityTrend communityTrend = CommunityTrend.of(
                    List.of("힐링", "자기계발", "소설", "에세이", "역사"),
                    "힐링 도서가 인기입니다",
                    List.of()
            );
            when(communityTrendUseCase.findCachedTrend()).thenReturn(Optional.of(communityTrend));

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).keyword()).isEqualTo("힐링");
            assertThat(result.get(1).keyword()).isEqualTo("자기계발");
        }

        @Test
        @DisplayName("폴백 시 null 및 빈 키워드 필터링")
        void getTrendingKeywordsByWindow_FallbackFiltersNullAndBlank() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 10;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);

            CommunityTrend communityTrend = CommunityTrend.of(
                    Arrays.asList("힐링", null, "  ", "자기계발", ""),
                    "힐링 도서가 인기입니다",
                    List.of()
            );
            when(communityTrendUseCase.findCachedTrend()).thenReturn(Optional.of(communityTrend));

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).keyword()).isEqualTo("힐링");
            assertThat(result.get(0).rank()).isEqualTo(1);
            assertThat(result.get(1).keyword()).isEqualTo("자기계발");
            assertThat(result.get(1).rank()).isEqualTo(2);
        }

        @Test
        @DisplayName("폴백 시 키워드가 null인 CommunityTrend는 빈 목록 반환")
        void getTrendingKeywordsByWindow_FallbackHandlesNullKeywords() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 10;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);

            CommunityTrend communityTrend = new CommunityTrend(null, "요약", List.of(), LocalDateTime.now());
            when(communityTrendUseCase.findCachedTrend()).thenReturn(Optional.of(communityTrend));

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("폴백 시 빈 키워드 목록은 빈 목록 반환")
        void getTrendingKeywordsByWindow_FallbackHandlesEmptyKeywords() {
            // Given
            TimeWindow window = TimeWindow.HOURLY;
            int limit = 10;

            when(trendingPort.getTopKeywordsByWindow(window, limit)).thenReturn(List.of());
            when(communityTrendUseCaseProvider.getIfAvailable()).thenReturn(communityTrendUseCase);

            CommunityTrend communityTrend = CommunityTrend.of(List.of(), "요약", List.of());
            when(communityTrendUseCase.findCachedTrend()).thenReturn(Optional.of(communityTrend));

            // When
            List<TrendingKeyword> result = searchKeywordTrendingService.getTrendingKeywordsByWindow(window, limit);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("점수 계산 테스트")
    class ScoreCalculationTests {

        @Test
        @DisplayName("최근 1시간 이내 검색 - 가중치 1.5x")
        void calculateScore_Within1Hour() {
            // Given - 현재 시간으로 검색 쿼리 생성 (1시간 이내)
            SearchQuery recentQuery = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", 0, null, null, "web", now
            );

            ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

            // When
            searchKeywordTrendingService.logSearchQuery(recentQuery);

            // Then
            verify(trendingPort).incrementKeywordScore(anyString(), scoreCaptor.capture(), any());
            // 기본 점수 1.0 * 시간 가중치 1.5 * 결과 가중치 1.0 = 1.5
            assertThat(scoreCaptor.getValue()).isCloseTo(1.5, within(0.0001));
        }

        @Test
        @DisplayName("결과 있음 - 가중치 1.2x")
        void calculateScore_WithResults() {
            // Given
            SearchQuery queryWithResults = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", 10, null, null, "web", now
            );

            ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

            // When
            searchKeywordTrendingService.logSearchQuery(queryWithResults);

            // Then
            verify(trendingPort).incrementKeywordScore(anyString(), scoreCaptor.capture(), any());
            // 기본 점수 1.0 * 시간 가중치 1.5 * 결과 가중치 1.2 = 1.8
            assertThat(scoreCaptor.getValue()).isCloseTo(1.8, within(0.0001));
        }

        @Test
        @DisplayName("결과 없음 - 가중치 1.0x")
        void calculateScore_WithoutResults() {
            // Given
            SearchQuery queryWithoutResults = new SearchQuery(
                    1L, 100L, "session-123", "test", "test", 0, null, null, "web", now
            );

            ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);

            // When
            searchKeywordTrendingService.logSearchQuery(queryWithoutResults);

            // Then
            verify(trendingPort).incrementKeywordScore(anyString(), scoreCaptor.capture(), any());
            // 기본 점수 1.0 * 시간 가중치 1.5 * 결과 가중치 1.0 = 1.5
            assertThat(scoreCaptor.getValue()).isCloseTo(1.5, within(0.0001));
        }
    }
}
