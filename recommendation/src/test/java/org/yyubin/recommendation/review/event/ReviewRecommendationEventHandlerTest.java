package org.yyubin.recommendation.review.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.recommendation.review.HighlightReviewRecommendationService;
import org.yyubin.recommendation.review.RecommendationIngestCommand;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewRecommendationEventHandler 테스트")
class ReviewRecommendationEventHandlerTest {

    @Mock
    private HighlightReviewRecommendationService reviewRecommendationService;

    @Mock
    private HighlightNormalizer highlightNormalizer;

    @InjectMocks
    private ReviewRecommendationEventHandler handler;

    @Captor
    private ArgumentCaptor<RecommendationIngestCommand> commandCaptor;

    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("null 이벤트는 무시")
    void handle_NullEvent_Ignored() {
        // When
        handler.handle(null);

        // Then
        verifyNoInteractions(reviewRecommendationService);
        verifyNoInteractions(highlightNormalizer);
    }

    @Test
    @DisplayName("type이 null인 이벤트는 무시")
    void handle_NullEventType_Ignored() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                null, 1L, 1L, 1L, "title", "summary", "content",
                List.of(), List.of(), List.of(), "genre", now, 5
        );

        // When
        handler.handle(event);

        // Then
        verifyNoInteractions(reviewRecommendationService);
    }

    @Test
    @DisplayName("DELETE 이벤트 처리 - 리뷰 삭제")
    void handle_DeleteEvent_DeletesReview() {
        // Given
        Long reviewId = 100L;
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.DELETE, reviewId, 1L, 1L, null, null, null,
                null, null, null, null, null, null
        );

        // When
        handler.handle(event);

        // Then
        verify(reviewRecommendationService).deleteReview(reviewId);
        verify(reviewRecommendationService, never()).ingest(any());
    }

    @Test
    @DisplayName("DELETE 이벤트 - reviewId가 null이면 삭제 안함")
    void handle_DeleteEventWithNullReviewId_NoDelete() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.DELETE, null, 1L, 1L, null, null, null,
                null, null, null, null, null, null
        );

        // When
        handler.handle(event);

        // Then
        verify(reviewRecommendationService, never()).deleteReview(any());
        verify(reviewRecommendationService, never()).ingest(any());
    }

    @Test
    @DisplayName("UPSERT 이벤트 - 필수 ID가 없으면 무시")
    void handle_UpsertEventMissingIds_Ignored() {
        // Given - reviewId null
        ReviewSearchIndexEvent event1 = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, null, 1L, 1L, "title", "summary", "content",
                List.of(), List.of(), List.of(), "genre", now, 5
        );

        // Given - userId null
        ReviewSearchIndexEvent event2 = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, null, 1L, "title", "summary", "content",
                List.of(), List.of(), List.of(), "genre", now, 5
        );

        // Given - bookId null
        ReviewSearchIndexEvent event3 = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 1L, null, "title", "summary", "content",
                List.of(), List.of(), List.of(), "genre", now, 5
        );

        // When
        handler.handle(event1);
        handler.handle(event2);
        handler.handle(event3);

        // Then
        verify(reviewRecommendationService, never()).ingest(any());
    }

    @Test
    @DisplayName("UPSERT 이벤트 - 정상 처리")
    void handle_UpsertEvent_IngestsCommand() {
        // Given
        List<String> highlights = List.of("highlight1", "highlight2");
        List<String> highlightsNorm = List.of("norm1", "norm2");
        List<String> keywords = List.of("keyword1");

        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 2L, 3L, "Book Title", "Summary",
                "Content", highlights, highlightsNorm, keywords, "Fiction", now, 5
        );

        // When
        handler.handle(event);

        // Then
        verify(reviewRecommendationService).ingest(commandCaptor.capture());
        RecommendationIngestCommand command = commandCaptor.getValue();

        assertThat(command.reviewId()).isEqualTo(1L);
        assertThat(command.userId()).isEqualTo(2L);
        assertThat(command.bookId()).isEqualTo(3L);
        assertThat(command.bookTitle()).isEqualTo("Book Title");
        assertThat(command.summary()).isEqualTo("Summary");
        assertThat(command.content()).isEqualTo("Content");
        assertThat(command.highlights()).containsExactly("highlight1", "highlight2");
        assertThat(command.highlightsNorm()).containsExactly("norm1", "norm2");
        assertThat(command.keywords()).containsExactly("keyword1");
        assertThat(command.genre()).isEqualTo("Fiction");
        assertThat(command.createdAt()).isEqualTo(now);
        assertThat(command.rating()).isEqualTo(5);
    }

    @Test
    @DisplayName("UPSERT 이벤트 - null highlights는 빈 리스트로 처리")
    void handle_UpsertEventNullHighlights_EmptyList() {
        // Given
        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 2L, 3L, "Title", "Summary",
                "Content", null, null, null, "genre", now, 4
        );

        // When
        handler.handle(event);

        // Then
        verify(reviewRecommendationService).ingest(commandCaptor.capture());
        RecommendationIngestCommand command = commandCaptor.getValue();

        assertThat(command.highlights()).isEmpty();
        assertThat(command.highlightsNorm()).isEmpty();
        assertThat(command.keywords()).isEmpty();
    }

    @Test
    @DisplayName("UPSERT 이벤트 - highlightsNorm이 비어있고 highlights가 있으면 정규화")
    void handle_UpsertEventEmptyHighlightsNorm_Normalizes() {
        // Given
        List<String> highlights = List.of("Original Highlight", "Another One");
        when(highlightNormalizer.normalize("Original Highlight")).thenReturn("original highlight");
        when(highlightNormalizer.normalize("Another One")).thenReturn("another one");

        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 2L, 3L, "Title", "Summary",
                "Content", highlights, List.of(), List.of(), "genre", now, 4
        );

        // When
        handler.handle(event);

        // Then
        verify(highlightNormalizer).normalize("Original Highlight");
        verify(highlightNormalizer).normalize("Another One");

        verify(reviewRecommendationService).ingest(commandCaptor.capture());
        RecommendationIngestCommand command = commandCaptor.getValue();
        assertThat(command.highlightsNorm()).containsExactly("original highlight", "another one");
    }

    @Test
    @DisplayName("UPSERT 이벤트 - highlightsNorm이 null이고 highlights가 있으면 정규화")
    void handle_UpsertEventNullHighlightsNorm_Normalizes() {
        // Given
        List<String> highlights = List.of("Test Highlight");
        when(highlightNormalizer.normalize("Test Highlight")).thenReturn("test highlight");

        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 2L, 3L, "Title", "Summary",
                "Content", highlights, null, List.of(), "genre", now, 4
        );

        // When
        handler.handle(event);

        // Then
        verify(highlightNormalizer).normalize("Test Highlight");
        verify(reviewRecommendationService).ingest(commandCaptor.capture());
        assertThat(commandCaptor.getValue().highlightsNorm()).containsExactly("test highlight");
    }

    @Test
    @DisplayName("UPSERT 이벤트 - highlights에 null 또는 빈 값은 필터링")
    void handle_UpsertEventFilterNullAndBlankHighlights() {
        // Given
        List<String> highlights = List.of("Valid", "", "  ");
        when(highlightNormalizer.normalize("Valid")).thenReturn("valid");

        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 2L, 3L, "Title", "Summary",
                "Content", highlights, List.of(), List.of(), "genre", now, 4
        );

        // When
        handler.handle(event);

        // Then
        verify(highlightNormalizer, times(1)).normalize(anyString());
        verify(highlightNormalizer).normalize("Valid");

        verify(reviewRecommendationService).ingest(commandCaptor.capture());
        assertThat(commandCaptor.getValue().highlightsNorm()).containsExactly("valid");
    }

    @Test
    @DisplayName("UPSERT 이벤트 - highlightsNorm이 이미 있으면 정규화하지 않음")
    void handle_UpsertEventWithHighlightsNorm_NoNormalization() {
        // Given
        List<String> highlights = List.of("highlight1");
        List<String> highlightsNorm = List.of("already normalized");

        ReviewSearchIndexEvent event = new ReviewSearchIndexEvent(
                ReviewSearchIndexEventType.UPSERT, 1L, 2L, 3L, "Title", "Summary",
                "Content", highlights, highlightsNorm, List.of(), "genre", now, 4
        );

        // When
        handler.handle(event);

        // Then
        verify(highlightNormalizer, never()).normalize(anyString());
        verify(reviewRecommendationService).ingest(commandCaptor.capture());
        assertThat(commandCaptor.getValue().highlightsNorm()).containsExactly("already normalized");
    }
}
