package org.yyubin.recommendation.review.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.yyubin.recommendation.review.HighlightRecommendationResult;
import org.yyubin.recommendation.review.HighlightReviewRecommendationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HighlightReviewRecommendationController 테스트")
class HighlightReviewRecommendationControllerTest {

    @Mock
    private HighlightReviewRecommendationService reviewRecommendationService;

    @InjectMocks
    private HighlightReviewRecommendationController controller;

    @Test
    @DisplayName("하이라이트로 추천 조회 - 정상 케이스")
    void recommendByHighlight_Success() {
        // Given
        String highlight = "인상깊은 구절";
        Long cursor = null;
        int size = 20;
        List<Long> reviewIds = List.of(1L, 2L, 3L);
        HighlightRecommendationResult expectedResult = new HighlightRecommendationResult(reviewIds, null);

        when(reviewRecommendationService.recommendByHighlight(highlight, cursor, size))
                .thenReturn(expectedResult);

        // When
        ResponseEntity<HighlightRecommendationResult> response = controller.recommendByHighlight(highlight, cursor, size);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().reviewIds()).containsExactly(1L, 2L, 3L);
        assertThat(response.getBody().nextCursor()).isNull();

        verify(reviewRecommendationService).recommendByHighlight(highlight, cursor, size);
    }

    @Test
    @DisplayName("하이라이트로 추천 조회 - 커서와 함께")
    void recommendByHighlight_WithCursor() {
        // Given
        String highlight = "인상깊은 구절";
        Long cursor = 100L;
        int size = 10;
        List<Long> reviewIds = List.of(50L, 40L, 30L);
        Long nextCursor = 25L;
        HighlightRecommendationResult expectedResult = new HighlightRecommendationResult(reviewIds, nextCursor);

        when(reviewRecommendationService.recommendByHighlight(highlight, cursor, size))
                .thenReturn(expectedResult);

        // When
        ResponseEntity<HighlightRecommendationResult> response = controller.recommendByHighlight(highlight, cursor, size);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().reviewIds()).containsExactly(50L, 40L, 30L);
        assertThat(response.getBody().nextCursor()).isEqualTo(25L);

        verify(reviewRecommendationService).recommendByHighlight(highlight, cursor, size);
    }

    @Test
    @DisplayName("하이라이트로 추천 조회 - 기본 size 사용")
    void recommendByHighlight_DefaultSize() {
        // Given
        String highlight = "테스트 구절";
        Long cursor = null;
        int defaultSize = 20;
        List<Long> reviewIds = List.of(1L, 2L);
        HighlightRecommendationResult expectedResult = new HighlightRecommendationResult(reviewIds, null);

        when(reviewRecommendationService.recommendByHighlight(highlight, cursor, defaultSize))
                .thenReturn(expectedResult);

        // When
        ResponseEntity<HighlightRecommendationResult> response = controller.recommendByHighlight(highlight, cursor, defaultSize);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(reviewRecommendationService).recommendByHighlight(highlight, cursor, defaultSize);
    }

    @Test
    @DisplayName("하이라이트로 추천 조회 - 빈 결과")
    void recommendByHighlight_EmptyResult() {
        // Given
        String highlight = "존재하지 않는 구절";
        Long cursor = null;
        int size = 20;
        HighlightRecommendationResult expectedResult = new HighlightRecommendationResult(List.of(), null);

        when(reviewRecommendationService.recommendByHighlight(highlight, cursor, size))
                .thenReturn(expectedResult);

        // When
        ResponseEntity<HighlightRecommendationResult> response = controller.recommendByHighlight(highlight, cursor, size);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().reviewIds()).isEmpty();
        assertThat(response.getBody().nextCursor()).isNull();
    }

    @Test
    @DisplayName("하이라이트로 추천 조회 - 서비스 예외 발생")
    void recommendByHighlight_ServiceException() {
        // Given
        String highlight = "";
        Long cursor = null;
        int size = 20;

        when(reviewRecommendationService.recommendByHighlight(highlight, cursor, size))
                .thenThrow(new IllegalArgumentException("Highlight must not be empty"));

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> controller.recommendByHighlight(highlight, cursor, size));

        verify(reviewRecommendationService).recommendByHighlight(highlight, cursor, size);
    }

    @Test
    @DisplayName("하이라이트로 추천 조회 - 다양한 size 값")
    void recommendByHighlight_VariousSizes() {
        // Given
        String highlight = "테스트";
        HighlightRecommendationResult result = new HighlightRecommendationResult(List.of(1L), null);

        when(reviewRecommendationService.recommendByHighlight(anyString(), any(), anyInt()))
                .thenReturn(result);

        // When & Then
        // size = 1
        controller.recommendByHighlight(highlight, null, 1);
        verify(reviewRecommendationService).recommendByHighlight(highlight, null, 1);

        // size = 50
        controller.recommendByHighlight(highlight, null, 50);
        verify(reviewRecommendationService).recommendByHighlight(highlight, null, 50);

        // size = 100
        controller.recommendByHighlight(highlight, null, 100);
        verify(reviewRecommendationService).recommendByHighlight(highlight, null, 100);
    }
}
