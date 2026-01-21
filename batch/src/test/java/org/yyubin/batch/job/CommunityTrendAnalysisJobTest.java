package org.yyubin.batch.job;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.recommendation.usecase.AnalyzeCommunityTrendUseCase;
import org.yyubin.domain.recommendation.CommunityTrend;
import org.yyubin.domain.recommendation.CommunityTrend.TrendingGenre;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommunityTrendAnalysisJob 테스트")
class CommunityTrendAnalysisJobTest {

    @Mock
    private AnalyzeCommunityTrendUseCase useCase;

    @InjectMocks
    private CommunityTrendAnalysisJob communityTrendAnalysisJob;

    @Test
    @DisplayName("커뮤니티 트렌드 분석 성공")
    void analyzeCommunityTrend_Success() {
        // Given
        CommunityTrend trend = new CommunityTrend(
                List.of("fantasy", "romance", "mystery"),
                "요즘 판타지와 로맨스가 인기입니다",
                List.of(
                        TrendingGenre.of("FANTASY", 0.4, "상승세"),
                        TrendingGenre.of("ROMANCE", 0.3, "안정")
                ),
                LocalDateTime.now()
        );
        when(useCase.execute()).thenReturn(trend);

        // When
        communityTrendAnalysisJob.analyzeCommunityTrend();

        // Then
        verify(useCase).execute();
    }

    @Test
    @DisplayName("빈 트렌드 결과 처리")
    void analyzeCommunityTrend_EmptyResult() {
        // Given
        CommunityTrend emptyTrend = new CommunityTrend(
                List.of(),
                "",
                List.of(),
                LocalDateTime.now()
        );
        when(useCase.execute()).thenReturn(emptyTrend);

        // When
        communityTrendAnalysisJob.analyzeCommunityTrend();

        // Then
        verify(useCase).execute();
    }

    @Test
    @DisplayName("분석 중 예외 발생 시 예외 전파")
    void analyzeCommunityTrend_ThrowsException() {
        // Given
        when(useCase.execute()).thenThrow(new RuntimeException("Analysis failed"));

        // When & Then
        assertThatThrownBy(() -> communityTrendAnalysisJob.analyzeCommunityTrend())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Analysis failed");

        verify(useCase).execute();
    }

    @Test
    @DisplayName("대량 키워드 트렌드 결과 처리")
    void analyzeCommunityTrend_LargeKeywordSet() {
        // Given
        List<String> keywords = java.util.stream.IntStream.rangeClosed(1, 100)
                .mapToObj(i -> "keyword" + i)
                .toList();
        List<TrendingGenre> genres = List.of(
                TrendingGenre.of("FANTASY", 0.25, "상승세"),
                TrendingGenre.of("ROMANCE", 0.20, "안정"),
                TrendingGenre.of("MYSTERY", 0.15, "하락세"),
                TrendingGenre.of("SCIENCE_FICTION", 0.10, "상승세")
        );
        CommunityTrend largeTrend = new CommunityTrend(
                keywords,
                "다양한 장르가 인기를 끌고 있습니다",
                genres,
                LocalDateTime.now()
        );
        when(useCase.execute()).thenReturn(largeTrend);

        // When
        communityTrendAnalysisJob.analyzeCommunityTrend();

        // Then
        verify(useCase).execute();
    }
}
