package org.yyubin.recommendation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ReviewElasticsearchCandidateGenerator;
import org.yyubin.recommendation.candidate.ReviewNeo4jCandidateGenerator;
import org.yyubin.recommendation.candidate.ReviewRecommendationCandidate;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.scoring.review.ReviewHybridScorer;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRecommendationService 테스트")
class ReviewRecommendationServiceTest {

    @Mock
    private ReviewElasticsearchCandidateGenerator elasticsearchCandidateGenerator;

    @Mock
    private ReviewNeo4jCandidateGenerator neo4jCandidateGenerator;

    @Mock
    private ReviewHybridScorer hybridScorer;

    @Mock
    private ReviewRecommendationCacheService cacheService;

    @Mock
    private ReviewRecommendationExposureService exposureService;

    @Mock
    private ReviewRecommendationProperties properties;

    @Mock
    private ReviewRecommendationProperties.Search searchConfig;

    @InjectMocks
    private ReviewRecommendationService reviewRecommendationService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getSearch()).thenReturn(searchConfig);
        lenient().when(properties.getMaxCandidates()).thenReturn(300);
        lenient().when(searchConfig.getFeedGraphRatio()).thenReturn(0.3);
    }

    @Nested
    @DisplayName("recommendFeed 테스트")
    class RecommendFeedTest {

        @Test
        @DisplayName("캐시가 있으면 캐시를 사용한다")
        void feed_WithCache_UsesCache() {
            // Given
            Long userId = 1L;
            int limit = 10;

            when(cacheService.exists(userId, null)).thenReturn(true);
            when(cacheService.get(userId, null, null, limit))
                    .thenReturn(List.of(
                            ReviewRecommendationResult.builder().reviewId(100L).score(0.9).rank(1).build()
                    ));

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            verify(exposureService).recordExposure(eq(userId), anyList());
            verify(neo4jCandidateGenerator, never()).generateFeedCandidates(anyLong(), anyInt());
        }

        @Test
        @DisplayName("forceRefresh시 캐시를 무시한다")
        void feed_ForceRefresh_IgnoresCache() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9)
            );

            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt()))
                    .thenReturn(candidates);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt()))
                    .thenReturn(List.of());
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, true);

            // Then
            assertThat(results).hasSize(1);
            verify(cacheService, never()).exists(anyLong(), any());
        }

        @Test
        @DisplayName("후보가 없으면 빈 리스트를 반환한다")
        void feed_NoCandidates_ReturnsEmpty() {
            // Given
            Long userId = 1L;
            int limit = 10;

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("중복 후보를 제거하고 점수가 높은 것을 유지한다")
        void feed_RemovesDuplicates_KeepsHigherScore() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> neo4jCandidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER, 0.9)
            );
            List<ReviewRecommendationCandidate> esCandidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.5)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(neo4jCandidates);
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(esCandidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getSource()).isEqualTo("GRAPH_SIMILAR_USER");
        }

        @Test
        @DisplayName("최근 노출된 리뷰를 필터링한다")
        void feed_FiltersRecentlyExposed() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, 201L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.8),
                    createCandidate(102L, 202L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.7)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);

            // 100L은 최근에 노출됨
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of(100L));

            Map<Long, Double> scores = new HashMap<>();
            scores.put(101L, 0.85);
            scores.put(102L, 0.75);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.stream().map(ReviewRecommendationResult::getReviewId))
                    .doesNotContain(100L);
        }

        @Test
        @DisplayName("노출 필터링 후 빈 리스트면 원본 후보를 사용한다")
        void feed_AllFiltered_UsesOriginalCandidates() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);

            // 모든 후보가 최근에 노출됨
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of(100L));

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getReviewId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("결과를 캐시에 저장한다")
        void feed_SavesCache() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            verify(cacheService).save(eq(userId), isNull(), anyMap());
        }

        @Test
        @DisplayName("노출을 기록한다")
        void feed_RecordsExposure() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            verify(exposureService).recordExposure(eq(userId), eq(List.of(100L)));
        }

        @Test
        @DisplayName("cursor 기반 페이징이 동작한다")
        void feed_CursorPagination() {
            // Given
            Long userId = 1L;
            Long cursor = 100L;
            int limit = 2;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, 201L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.8),
                    createCandidate(102L, 202L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.7)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = new HashMap<>();
            scores.put(100L, 0.95);
            scores.put(101L, 0.85);
            scores.put(102L, 0.75);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, cursor, limit, false);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getReviewId()).isEqualTo(101L);
            assertThat(results.get(1).getReviewId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("결과에 순위를 매긴다")
        void feed_AssignsRanks() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, 201L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.8)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = new HashMap<>();
            scores.put(100L, 0.95);
            scores.put(101L, 0.85);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getRank()).isEqualTo(1);
            assertThat(results.get(1).getRank()).isEqualTo(2);
        }

        @Test
        @DisplayName("null reviewId를 가진 후보는 필터링한다")
        void feed_FiltersNullReviewId() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(null, 200L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, 201L, ReviewRecommendationCandidate.CandidateSource.POPULARITY, 0.8)
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = Map.of(101L, 0.85);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getReviewId()).isEqualTo(101L);
        }
    }

    @Nested
    @DisplayName("recommendForBook 테스트")
    class RecommendForBookTest {

        @Test
        @DisplayName("특정 도서의 리뷰를 추천한다")
        void forBook_Success() {
            // Given
            Long userId = 1L;
            Long bookId = 500L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, bookId, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.9)
            );

            when(cacheService.exists(userId, bookId)).thenReturn(false);
            when(elasticsearchCandidateGenerator.generateBookScopedCandidates(eq(bookId), anyInt()))
                    .thenReturn(candidates);

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), eq(bookId), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendForBook(userId, bookId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getBookId()).isEqualTo(bookId);
        }

        @Test
        @DisplayName("캐시가 있으면 캐시를 사용한다")
        void forBook_WithCache_UsesCache() {
            // Given
            Long userId = 1L;
            Long bookId = 500L;
            int limit = 10;

            when(cacheService.exists(userId, bookId)).thenReturn(true);
            when(cacheService.get(userId, bookId, null, limit))
                    .thenReturn(List.of(
                            ReviewRecommendationResult.builder().reviewId(100L).bookId(bookId).score(0.9).rank(1).build()
                    ));

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendForBook(userId, bookId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            // 도서 컨텍스트에서는 노출 기록을 하지 않음
            verify(exposureService, never()).recordExposure(anyLong(), anyList());
        }

        @Test
        @DisplayName("도서 컨텍스트에서는 노출 필터링을 하지 않는다")
        void forBook_NoExposureFiltering() {
            // Given
            Long userId = 1L;
            Long bookId = 500L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, bookId, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.9)
            );

            when(cacheService.exists(userId, bookId)).thenReturn(false);
            when(elasticsearchCandidateGenerator.generateBookScopedCandidates(eq(bookId), anyInt()))
                    .thenReturn(candidates);

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), eq(bookId), anyList())).thenReturn(scores);

            // When
            reviewRecommendationService.recommendForBook(userId, bookId, limit, false);

            // Then
            verify(exposureService, never()).loadRecentReviewIds(anyLong());
            verify(exposureService, never()).recordExposure(anyLong(), anyList());
        }

        @Test
        @DisplayName("cursor 기반 페이징이 동작한다")
        void forBook_CursorPagination() {
            // Given
            Long userId = 1L;
            Long bookId = 500L;
            Long cursor = 100L;
            int limit = 2;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, bookId, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.9),
                    createCandidate(101L, bookId, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.8),
                    createCandidate(102L, bookId, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.7)
            );

            when(cacheService.exists(userId, bookId)).thenReturn(false);
            when(elasticsearchCandidateGenerator.generateBookScopedCandidates(eq(bookId), anyInt()))
                    .thenReturn(candidates);

            Map<Long, Double> scores = new HashMap<>();
            scores.put(100L, 0.95);
            scores.put(101L, 0.85);
            scores.put(102L, 0.75);
            when(hybridScorer.batchCalculate(eq(userId), eq(bookId), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendForBook(userId, bookId, cursor, limit, false);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getReviewId()).isEqualTo(101L);
            assertThat(results.get(1).getReviewId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("결과를 캐시에 저장한다")
        void forBook_SavesCache() {
            // Given
            Long userId = 1L;
            Long bookId = 500L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, bookId, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR, 0.9)
            );

            when(cacheService.exists(userId, bookId)).thenReturn(false);
            when(elasticsearchCandidateGenerator.generateBookScopedCandidates(eq(bookId), anyInt()))
                    .thenReturn(candidates);

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), eq(bookId), anyList())).thenReturn(scores);

            // When
            reviewRecommendationService.recommendForBook(userId, bookId, limit, false);

            // Then
            verify(cacheService).save(eq(userId), eq(bookId), anyMap());
        }
    }

    @Nested
    @DisplayName("null 후보 필드 처리 테스트")
    class NullCandidateFieldsTest {

        @Test
        @DisplayName("candidate가 null인 경우에도 결과를 생성한다")
        void handleNullCandidate() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<ReviewRecommendationCandidate> candidates = List.of(
                    createCandidate(100L, 200L, null, null) // source와 score가 null
            );

            when(cacheService.exists(userId, null)).thenReturn(false);
            when(neo4jCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateFeedCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(exposureService.loadRecentReviewIds(userId)).thenReturn(Set.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), isNull(), anyList())).thenReturn(scores);

            // When
            List<ReviewRecommendationResult> results = reviewRecommendationService.recommendFeed(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getReviewId()).isEqualTo(100L);
            assertThat(results.get(0).getSource()).isNull();
        }
    }

    private ReviewRecommendationCandidate createCandidate(
            Long reviewId,
            Long bookId,
            ReviewRecommendationCandidate.CandidateSource source,
            Double score
    ) {
        return ReviewRecommendationCandidate.builder()
                .reviewId(reviewId)
                .bookId(bookId)
                .source(source)
                .initialScore(score)
                .reason("Test reason")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
