package org.yyubin.recommendation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.recommendation.AnalyzeUserPreferenceUseCase;
import org.yyubin.application.recommendation.usecase.GenerateRecommendationExplanationUseCase;
import org.yyubin.domain.recommendation.RecommendationExplanation;
import org.yyubin.domain.recommendation.UserAnalysis;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIEnrichmentService 테스트")
class AIEnrichmentServiceTest {

    @Mock
    private AnalyzeUserPreferenceUseCase analyzeUserPreferenceUseCase;

    @Mock
    private GenerateRecommendationExplanationUseCase generateRecommendationExplanationUseCase;

    @InjectMocks
    private AIEnrichmentService aiEnrichmentService;

    @Nested
    @DisplayName("analyzeUserPreference 테스트")
    class AnalyzeUserPreferenceTest {

        @Test
        @DisplayName("AI 활성화시 사용자 취향 분석을 반환한다")
        void analyze_WhenEnabled_ReturnsAnalysis() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);
            Long userId = 1L;

            UserAnalysis analysis = UserAnalysis.of(
                    userId,
                    "fantasy_enthusiast",
                    "판타지 장르를 선호합니다",
                    List.of("모험", "마법"),
                    List.of()
            );
            when(analyzeUserPreferenceUseCase.execute(userId)).thenReturn(analysis);

            // When
            Optional<UserAnalysis> result = aiEnrichmentService.analyzeUserPreference(userId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().personaType()).isEqualTo("fantasy_enthusiast");
            verify(analyzeUserPreferenceUseCase).execute(userId);
        }

        @Test
        @DisplayName("AI 비활성화시 빈 Optional을 반환한다")
        void analyze_WhenDisabled_ReturnsEmpty() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", false);
            Long userId = 1L;

            // When
            Optional<UserAnalysis> result = aiEnrichmentService.analyzeUserPreference(userId);

            // Then
            assertThat(result).isEmpty();
            verify(analyzeUserPreferenceUseCase, never()).execute(anyLong());
        }

        @Test
        @DisplayName("예외 발생시 빈 Optional을 반환한다")
        void analyze_WhenException_ReturnsEmpty() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);
            Long userId = 1L;

            when(analyzeUserPreferenceUseCase.execute(userId))
                    .thenThrow(new RuntimeException("AI service error"));

            // When
            Optional<UserAnalysis> result = aiEnrichmentService.analyzeUserPreference(userId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateExplanation 테스트")
    class GenerateExplanationTest {

        @Test
        @DisplayName("AI 활성화시 추천 설명을 생성한다")
        void generate_WhenEnabled_ReturnsExplanation() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);
            Long userId = 1L;
            Long bookId = 100L;
            String bookTitle = "Test Book";
            Map<String, String> scoreDetails = Map.of("추천점수", "0.95");

            RecommendationExplanation explanation = RecommendationExplanation.of(
                    userId,
                    bookId,
                    "이 책을 추천합니다",
                    Map.of("그래프", "비슷한 취향")
            );
            when(generateRecommendationExplanationUseCase.execute(eq(userId), eq(bookId), eq(bookTitle), anyMap()))
                    .thenReturn(explanation);

            // When
            Optional<RecommendationExplanation> result = aiEnrichmentService.generateExplanation(
                    userId, bookId, bookTitle, scoreDetails
            );

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().explanation()).isEqualTo("이 책을 추천합니다");
        }

        @Test
        @DisplayName("AI 비활성화시 빈 Optional을 반환한다")
        void generate_WhenDisabled_ReturnsEmpty() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", false);

            // When
            Optional<RecommendationExplanation> result = aiEnrichmentService.generateExplanation(
                    1L, 100L, "Test Book", Map.of()
            );

            // Then
            assertThat(result).isEmpty();
            verify(generateRecommendationExplanationUseCase, never()).execute(anyLong(), anyLong(), anyString(), anyMap());
        }

        @Test
        @DisplayName("예외 발생시 빈 Optional을 반환한다")
        void generate_WhenException_ReturnsEmpty() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);

            when(generateRecommendationExplanationUseCase.execute(anyLong(), anyLong(), anyString(), anyMap()))
                    .thenThrow(new RuntimeException("AI service error"));

            // When
            Optional<RecommendationExplanation> result = aiEnrichmentService.generateExplanation(
                    1L, 100L, "Test Book", Map.of()
            );

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("enrichRecommendations 테스트")
    class EnrichRecommendationsTest {

        @Test
        @DisplayName("AI 활성화시 추천 결과에 설명을 추가한다")
        void enrich_WhenEnabled_ReturnsExplanations() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);
            Long userId = 1L;

            List<RecommendationResult> results = List.of(
                    RecommendationResult.builder().bookId(100L).score(0.95).source("NEO4J").build(),
                    RecommendationResult.builder().bookId(101L).score(0.85).build()
            );

            RecommendationExplanation explanation1 = RecommendationExplanation.of(
                    userId, 100L, "설명1", Map.of()
            );
            RecommendationExplanation explanation2 = RecommendationExplanation.of(
                    userId, 101L, "설명2", Map.of()
            );

            when(generateRecommendationExplanationUseCase.execute(eq(userId), eq(100L), anyString(), anyMap()))
                    .thenReturn(explanation1);
            when(generateRecommendationExplanationUseCase.execute(eq(userId), eq(101L), anyString(), anyMap()))
                    .thenReturn(explanation2);

            // When
            Map<Long, RecommendationExplanation> explanations = aiEnrichmentService.enrichRecommendations(userId, results);

            // Then
            assertThat(explanations).hasSize(2);
            assertThat(explanations.get(100L).explanation()).isEqualTo("설명1");
            assertThat(explanations.get(101L).explanation()).isEqualTo("설명2");
        }

        @Test
        @DisplayName("AI 비활성화시 빈 맵을 반환한다")
        void enrich_WhenDisabled_ReturnsEmptyMap() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", false);

            List<RecommendationResult> results = List.of(
                    RecommendationResult.builder().bookId(100L).score(0.95).build()
            );

            // When
            Map<Long, RecommendationExplanation> explanations = aiEnrichmentService.enrichRecommendations(1L, results);

            // Then
            assertThat(explanations).isEmpty();
        }

        @Test
        @DisplayName("null 리스트는 빈 맵을 반환한다")
        void enrich_NullResults_ReturnsEmptyMap() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);

            // When
            Map<Long, RecommendationExplanation> explanations = aiEnrichmentService.enrichRecommendations(1L, null);

            // Then
            assertThat(explanations).isEmpty();
        }

        @Test
        @DisplayName("빈 리스트는 빈 맵을 반환한다")
        void enrich_EmptyResults_ReturnsEmptyMap() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);

            // When
            Map<Long, RecommendationExplanation> explanations = aiEnrichmentService.enrichRecommendations(1L, List.of());

            // Then
            assertThat(explanations).isEmpty();
        }

        @Test
        @DisplayName("일부 설명 생성 실패시 성공한 것만 반환한다")
        void enrich_PartialFailure_ReturnsSuccessful() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);
            Long userId = 1L;

            List<RecommendationResult> results = List.of(
                    RecommendationResult.builder().bookId(100L).score(0.95).build(),
                    RecommendationResult.builder().bookId(101L).score(0.85).build()
            );

            RecommendationExplanation explanation = RecommendationExplanation.of(
                    userId, 100L, "설명", Map.of()
            );

            when(generateRecommendationExplanationUseCase.execute(eq(userId), eq(100L), anyString(), anyMap()))
                    .thenReturn(explanation);
            when(generateRecommendationExplanationUseCase.execute(eq(userId), eq(101L), anyString(), anyMap()))
                    .thenThrow(new RuntimeException("AI error"));

            // When
            Map<Long, RecommendationExplanation> explanations = aiEnrichmentService.enrichRecommendations(userId, results);

            // Then
            assertThat(explanations).hasSize(1);
            assertThat(explanations.get(100L)).isNotNull();
            assertThat(explanations.get(101L)).isNull();
        }

        @Test
        @DisplayName("source가 있으면 scoreDetails에 포함한다")
        void enrich_WithSource_IncludesInScoreDetails() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);
            Long userId = 1L;

            List<RecommendationResult> results = List.of(
                    RecommendationResult.builder()
                            .bookId(100L)
                            .score(0.95)
                            .source("NEO4J_COLLABORATIVE")
                            .build()
            );

            when(generateRecommendationExplanationUseCase.execute(
                    eq(userId), eq(100L), anyString(), argThat(map ->
                            map.containsKey("출처") && "NEO4J_COLLABORATIVE".equals(map.get("출처"))
                    )
            )).thenReturn(RecommendationExplanation.of(userId, 100L, "설명", Map.of()));

            // When
            aiEnrichmentService.enrichRecommendations(userId, results);

            // Then
            verify(generateRecommendationExplanationUseCase).execute(
                    eq(userId), eq(100L), anyString(), argThat(map ->
                            map.containsKey("출처") && "NEO4J_COLLABORATIVE".equals(map.get("출처"))
                    )
            );
        }
    }

    @Nested
    @DisplayName("isEnabled 테스트")
    class IsEnabledTest {

        @Test
        @DisplayName("활성화 상태를 반환한다")
        void isEnabled_ReturnsTrue() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", true);

            // When & Then
            assertThat(aiEnrichmentService.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("비활성화 상태를 반환한다")
        void isEnabled_ReturnsFalse() {
            // Given
            ReflectionTestUtils.setField(aiEnrichmentService, "enabled", false);

            // When & Then
            assertThat(aiEnrichmentService.isEnabled()).isFalse();
        }
    }
}
