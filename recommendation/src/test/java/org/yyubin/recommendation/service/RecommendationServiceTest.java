package org.yyubin.recommendation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.recommendation.candidate.ElasticsearchCandidateGenerator;
import org.yyubin.recommendation.candidate.Neo4jCandidateGenerator;
import org.yyubin.recommendation.candidate.RecommendationCandidate;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.scoring.HybridScorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationService 테스트")
class RecommendationServiceTest {

    @Mock
    private Neo4jCandidateGenerator neo4jCandidateGenerator;

    @Mock
    private ElasticsearchCandidateGenerator elasticsearchCandidateGenerator;

    @Mock
    private HybridScorer hybridScorer;

    @Mock
    private RecommendationCacheService cacheService;

    @Mock
    private RecommendationProperties properties;

    @Mock
    private RecommendationProperties.SearchConfig searchConfig;

    @InjectMocks
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getSearch()).thenReturn(searchConfig);
        lenient().when(searchConfig.getMaxCandidates()).thenReturn(500);
    }

    @Nested
    @DisplayName("generateRecommendations 테스트")
    class GenerateRecommendationsTest {

        @Test
        @DisplayName("비로그인 사용자는 기본 추천을 반환한다")
        void generate_NonLoggedInUser_ReturnsDefault() {
            // Given
            Long userId = null;
            int limit = 10;

            List<RecommendationCandidate> popularCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, RecommendationCandidate.CandidateSource.POPULARITY, 0.8)
            );

            when(elasticsearchCandidateGenerator.generateCandidates(isNull(), anyInt()))
                    .thenReturn(popularCandidates);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, false);

            // Then
            assertThat(results).hasSize(2);
            verify(neo4jCandidateGenerator, never()).generateCandidates(anyLong(), anyInt());
        }

        @Test
        @DisplayName("캐시가 있으면 캐시를 사용한다")
        void generate_WithCache_UsesCache() {
            // Given
            Long userId = 1L;
            int limit = 10;

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(true);
            when(cacheService.getRecommendationsWithSampling(eq(userId), isNull(), eq(limit), isNull(), eq(false)))
                    .thenReturn(List.of(
                            RecommendationResult.builder().bookId(100L).score(0.9).rank(1).build()
                    ));

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            verify(neo4jCandidateGenerator, never()).generateCandidates(anyLong(), anyInt());
            verify(elasticsearchCandidateGenerator, never()).generateCandidates(anyLong(), anyInt());
        }

        @Test
        @DisplayName("forceRefresh시 캐시를 무시한다")
        void generate_ForceRefresh_IgnoresCache() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<RecommendationCandidate> candidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE, 0.9)
            );

            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, true);

            // Then
            assertThat(results).hasSize(1);
            verify(cacheService, never()).hasCachedRecommendations(anyLong());
            verify(neo4jCandidateGenerator).generateCandidates(eq(userId), anyInt());
        }

        @Test
        @DisplayName("후보가 없으면 기본 추천을 반환한다")
        void generate_NoCandidates_ReturnsDefault() {
            // Given
            Long userId = 1L;
            int limit = 10;

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());

            List<RecommendationCandidate> popularCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.POPULARITY, 0.9)
            );
            when(elasticsearchCandidateGenerator.generateCandidates(isNull(), anyInt()))
                    .thenReturn(popularCandidates);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("중복 후보를 제거하고 점수가 높은 것을 유지한다")
        void generate_RemovesDuplicates_KeepsHigherScore() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<RecommendationCandidate> neo4jCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE, 0.9)
            );
            List<RecommendationCandidate> esCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT, 0.5) // lower score
            );

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(neo4jCandidates);
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(esCandidates);

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getSource()).isEqualTo("NEO4J_COLLABORATIVE"); // higher initial score
        }

        @Test
        @DisplayName("결과를 캐시에 저장한다")
        void generate_SavesCache() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<RecommendationCandidate> candidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE, 0.9)
            );

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            recommendationService.generateRecommendations(userId, limit, false);

            // Then
            verify(cacheService).saveRecommendations(eq(userId), anyMap());
        }

        @Test
        @DisplayName("결과에 순위를 매긴다")
        void generate_AssignsRanks() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<RecommendationCandidate> candidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE, 0.9),
                    createCandidate(101L, RecommendationCandidate.CandidateSource.NEO4J_GENRE, 0.8)
            );

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());

            Map<Long, Double> scores = new HashMap<>();
            scores.put(100L, 0.95);
            scores.put(101L, 0.85);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, false);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getRank()).isEqualTo(1);
            assertThat(results.get(0).getBookId()).isEqualTo(100L);
            assertThat(results.get(1).getRank()).isEqualTo(2);
            assertThat(results.get(1).getBookId()).isEqualTo(101L);
        }

        @Test
        @DisplayName("cursor 기반 페이징이 동작한다")
        void generate_CursorPagination() {
            // Given
            Long userId = 1L;
            Long cursor = 100L;
            int limit = 2;

            List<RecommendationCandidate> candidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE, 0.9),
                    createCandidate(101L, RecommendationCandidate.CandidateSource.NEO4J_GENRE, 0.8),
                    createCandidate(102L, RecommendationCandidate.CandidateSource.POPULARITY, 0.7)
            );

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());

            Map<Long, Double> scores = new HashMap<>();
            scores.put(100L, 0.95);
            scores.put(101L, 0.85);
            scores.put(102L, 0.75);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, cursor, limit, false);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getBookId()).isEqualTo(101L);
            assertThat(results.get(1).getBookId()).isEqualTo(102L);
        }

        @Test
        @DisplayName("Neo4j 예외시에도 Elasticsearch 후보를 사용한다")
        void generate_Neo4jException_FallsBackToES() {
            // Given
            Long userId = 1L;
            int limit = 10;

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt()))
                    .thenThrow(new RuntimeException("Neo4j error"));

            List<RecommendationCandidate> esCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT, 0.8)
            );
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(esCandidates);

            Map<Long, Double> scores = Map.of(100L, 0.85);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(userId, limit, false);

            // Then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("샘플링 파라미터를 전달한다")
        void generate_WithSampling_PassesSamplingParams() {
            // Given
            Long userId = 1L;
            int limit = 10;
            String sessionId = "session-123";

            when(cacheService.hasCachedRecommendations(userId)).thenReturn(true);
            when(cacheService.getRecommendationsWithSampling(eq(userId), isNull(), eq(limit), eq(sessionId), eq(true)))
                    .thenReturn(List.of(
                            RecommendationResult.builder().bookId(100L).score(0.9).rank(1).build()
                    ));

            // When
            List<RecommendationResult> results = recommendationService.generateRecommendations(
                    userId, null, limit, false, true, sessionId
            );

            // Then
            verify(cacheService).getRecommendationsWithSampling(userId, null, limit, sessionId, true);
            assertThat(results).hasSize(1);
        }
    }

    @Nested
    @DisplayName("generateDefaultRecommendations 테스트")
    class GenerateDefaultRecommendationsTest {

        @Test
        @DisplayName("인기 도서 기반 기본 추천을 생성한다")
        void generateDefault_Success() {
            // Given
            int limit = 10;

            List<RecommendationCandidate> popularCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, RecommendationCandidate.CandidateSource.POPULARITY, 0.8)
            );

            when(elasticsearchCandidateGenerator.generateCandidates(isNull(), eq(limit)))
                    .thenReturn(popularCandidates);

            // When
            List<RecommendationResult> results = recommendationService.generateDefaultRecommendations(limit);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getBookId()).isEqualTo(100L);
            assertThat(results.get(0).getRank()).isEqualTo(1);
        }

        @Test
        @DisplayName("후보가 없으면 빈 리스트를 반환한다")
        void generateDefault_NoCandidates_ReturnsEmpty() {
            // Given
            when(elasticsearchCandidateGenerator.generateCandidates(isNull(), anyInt()))
                    .thenReturn(List.of());

            // When
            List<RecommendationResult> results = recommendationService.generateDefaultRecommendations(10);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("예외 발생시 빈 리스트를 반환한다")
        void generateDefault_Exception_ReturnsEmpty() {
            // Given
            when(elasticsearchCandidateGenerator.generateCandidates(isNull(), anyInt()))
                    .thenThrow(new RuntimeException("ES error"));

            // When
            List<RecommendationResult> results = recommendationService.generateDefaultRecommendations(10);

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("cursor 기반 페이징이 동작한다")
        void generateDefault_CursorPagination() {
            // Given
            Long cursor = 100L;
            int limit = 2;

            List<RecommendationCandidate> popularCandidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.POPULARITY, 0.9),
                    createCandidate(101L, RecommendationCandidate.CandidateSource.POPULARITY, 0.8),
                    createCandidate(102L, RecommendationCandidate.CandidateSource.POPULARITY, 0.7)
            );

            // cursor가 있으면 더 많이 가져옴
            when(elasticsearchCandidateGenerator.generateCandidates(isNull(), eq(6)))
                    .thenReturn(popularCandidates);

            // When
            List<RecommendationResult> results = recommendationService.generateDefaultRecommendations(cursor, limit);

            // Then
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getBookId()).isEqualTo(101L);
            assertThat(results.get(1).getBookId()).isEqualTo(102L);
        }
    }

    @Nested
    @DisplayName("getCachedRecommendations 테스트")
    class GetCachedRecommendationsTest {

        @Test
        @DisplayName("캐시된 추천을 조회한다")
        void getCached_Success() {
            // Given
            Long userId = 1L;
            int limit = 10;

            List<RecommendationResult> cached = List.of(
                    RecommendationResult.builder().bookId(100L).score(0.9).rank(1).build()
            );
            when(cacheService.getRecommendations(userId, limit)).thenReturn(cached);

            // When
            List<RecommendationResult> results = recommendationService.getCachedRecommendations(userId, limit);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getBookId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("cursor 기반 캐시 조회")
        void getCached_WithCursor() {
            // Given
            Long userId = 1L;
            Long cursor = 100L;
            int limit = 10;

            List<RecommendationResult> cached = List.of(
                    RecommendationResult.builder().bookId(101L).score(0.8).rank(2).build()
            );
            when(cacheService.getRecommendations(userId, cursor, limit)).thenReturn(cached);

            // When
            List<RecommendationResult> results = recommendationService.getCachedRecommendations(userId, cursor, limit);

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getBookId()).isEqualTo(101L);
        }
    }

    @Nested
    @DisplayName("getScoreBreakdown 테스트")
    class GetScoreBreakdownTest {

        @Test
        @DisplayName("점수 상세 정보를 조회한다")
        void getBreakdown_Success() {
            // Given
            Long userId = 1L;
            Long bookId = 100L;

            HybridScorer.ScoreBreakdown breakdown = HybridScorer.ScoreBreakdown.builder()
                    .graphScore(0.8)
                    .semanticScore(0.7)
                    .popularityScore(0.6)
                    .freshnessScore(0.5)
                    .finalScore(0.75)
                    .build();

            when(hybridScorer.getScoreBreakdown(eq(userId), any(RecommendationCandidate.class)))
                    .thenReturn(breakdown);

            // When
            HybridScorer.ScoreBreakdown result = recommendationService.getScoreBreakdown(userId, bookId);

            // Then
            assertThat(result.getFinalScore()).isEqualTo(0.75);
        }
    }

    @Nested
    @DisplayName("refreshRecommendations 테스트")
    class RefreshRecommendationsTest {

        @Test
        @DisplayName("캐시를 삭제하고 새로 생성한다")
        void refresh_ClearsAndRegenerates() {
            // Given
            Long userId = 1L;

            List<RecommendationCandidate> candidates = List.of(
                    createCandidate(100L, RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE, 0.9)
            );

            lenient().when(cacheService.hasCachedRecommendations(userId)).thenReturn(false);
            when(neo4jCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(candidates);
            when(elasticsearchCandidateGenerator.generateCandidates(eq(userId), anyInt())).thenReturn(List.of());

            Map<Long, Double> scores = Map.of(100L, 0.95);
            when(hybridScorer.batchCalculate(eq(userId), anyList())).thenReturn(scores);

            // When
            recommendationService.refreshRecommendations(userId);

            // Then
            verify(cacheService).clearRecommendations(userId);
            verify(cacheService).saveRecommendations(eq(userId), anyMap());
        }
    }

    @Nested
    @DisplayName("getStats 테스트")
    class GetStatsTest {

        @Test
        @DisplayName("통계 정보를 반환한다")
        void getStats_Success() {
            // Given
            Long userId = 1L;

            RecommendationCacheService.CacheStats cacheStats = RecommendationCacheService.CacheStats.builder()
                    .userId(userId)
                    .cachedItems(50L)
                    .ttlSeconds(3600L)
                    .exists(true)
                    .build();

            when(cacheService.getCacheStats(userId)).thenReturn(cacheStats);

            // When
            RecommendationService.RecommendationStats stats = recommendationService.getStats(userId);

            // Then
            assertThat(stats.getUserId()).isEqualTo(userId);
            assertThat(stats.getCachedItems()).isEqualTo(50L);
            assertThat(stats.getCacheTtlSeconds()).isEqualTo(3600L);
            assertThat(stats.isHasCachedRecommendations()).isTrue();
        }
    }

    @Nested
    @DisplayName("RecommendationStats 테스트")
    class RecommendationStatsTest {

        @Test
        @DisplayName("Builder로 RecommendationStats를 생성할 수 있다")
        void builder_Works() {
            // When
            RecommendationService.RecommendationStats stats = RecommendationService.RecommendationStats.builder()
                    .userId(1L)
                    .cachedItems(100L)
                    .cacheTtlSeconds(3600L)
                    .hasCachedRecommendations(true)
                    .build();

            // Then
            assertThat(stats.getUserId()).isEqualTo(1L);
            assertThat(stats.getCachedItems()).isEqualTo(100L);
            assertThat(stats.getCacheTtlSeconds()).isEqualTo(3600L);
            assertThat(stats.isHasCachedRecommendations()).isTrue();
        }
    }

    private RecommendationCandidate createCandidate(Long bookId, RecommendationCandidate.CandidateSource source, double score) {
        return RecommendationCandidate.builder()
                .bookId(bookId)
                .source(source)
                .initialScore(score)
                .reason("Test reason")
                .build();
    }
}
