package org.yyubin.recommendation.tracking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.*;
import org.yyubin.domain.user.UserId;
import org.yyubin.recommendation.config.RecommendationTrackingProperties;
import org.yyubin.recommendation.service.RecommendationCacheService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecommendationTrackingEventHandler 테스트")
class RecommendationTrackingEventHandlerTest {

    @Mock
    private RecommendationCacheService cacheService;

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private RecommendationTrackingProperties properties;

    @InjectMocks
    private RecommendationTrackingEventHandler handler;

    private RecommendationTrackingProperties.Weights weights;
    private RecommendationTrackingProperties.Caps caps;

    @BeforeEach
    void setUp() {
        weights = new RecommendationTrackingProperties.Weights();
        weights.setImpression(0.05);
        weights.setClick(0.3);
        weights.setDwellPerMs(0.001);
        weights.setScrollPerPct(0.005);
        weights.setBookmark(1.0);
        weights.setLike(0.6);
        weights.setFollow(0.8);
        weights.setReviewCreate(0.4);
        weights.setReviewUpdate(0.4);

        caps = new RecommendationTrackingProperties.Caps();
        caps.setDwellMax(1.5);
        caps.setScrollMax(0.5);

        lenient().when(properties.getWeights()).thenReturn(weights);
        lenient().when(properties.getCaps()).thenReturn(caps);
    }

    @Nested
    @DisplayName("handle 메서드 - 조기 반환 케이스")
    class HandleEarlyReturn {

        @Test
        @DisplayName("userId가 null이면 cacheService를 호출하지 않는다")
        void handle_NullUserId_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(null, "CLICK", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("eventType이 null이면 cacheService를 호출하지 않는다")
        void handle_NullEventType_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, null, "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("contentType이 null이면 cacheService를 호출하지 않는다")
        void handle_NullContentType_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", null, "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("contentId가 null이면 cacheService를 호출하지 않는다")
        void handle_NullContentId_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "BOOK", null, null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("알 수 없는 contentType이면 cacheService를 호출하지 않는다")
        void handle_UnknownContentType_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "UNKNOWN_TYPE", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("알 수 없는 eventType이면 가중치가 0이므로 cacheService를 호출하지 않는다")
        void handle_UnknownEventType_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "UNKNOWN_EVENT", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }
    }

    @Nested
    @DisplayName("handle 메서드 - BOOK contentType")
    class HandleBookContentType {

        @Test
        @DisplayName("BOOK contentType으로 CLICK 이벤트 처리")
        void handle_BookClick_CallsCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.3);
        }

        @Test
        @DisplayName("metadata에서 contentType과 contentId를 오버라이드할 수 있다")
        void handle_MetadataOverridesContentTypeAndId() {
            // Given
            Map<String, Object> metadata = Map.of(
                    "contentType", "BOOK",
                    "contentId", "200"
            );
            EventPayload payload = createPayload(1L, "CLICK", "REVIEW", "999", metadata);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 200L, 0.3);
        }
    }

    @Nested
    @DisplayName("handle 메서드 - REVIEW contentType")
    class HandleReviewContentType {

        @Test
        @DisplayName("REVIEW contentType으로 이벤트 처리 시 Review에서 BookId를 가져온다")
        void handle_ReviewClick_ResolvesBookIdFromReview() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "REVIEW", "50", null);
            Review review = createReview(50L, 1L, 100L);
            when(loadReviewPort.loadById(50L)).thenReturn(review);

            // When
            handler.handle(payload);

            // Then
            verify(loadReviewPort, times(1)).loadById(50L);
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.3);
        }

        @Test
        @DisplayName("Review 조회 실패 시 cacheService를 호출하지 않는다")
        void handle_ReviewLoadFails_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "REVIEW", "50", null);
            when(loadReviewPort.loadById(50L)).thenThrow(new RuntimeException("Review not found"));

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("유효하지 않은 contentId로 Review 조회 시 cacheService를 호출하지 않는다")
        void handle_InvalidReviewId_DoesNotCallCacheService() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "REVIEW", "invalid", null);

            // When
            handler.handle(payload);

            // Then
            verify(loadReviewPort, never()).loadById(anyLong());
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }
    }

    @Nested
    @DisplayName("이벤트 타입별 가중치 계산")
    class EventTypeWeights {

        @Test
        @DisplayName("IMPRESSION 이벤트 가중치")
        void handle_ImpressionEvent() {
            // Given
            EventPayload payload = createPayload(1L, "IMPRESSION", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.05);
        }

        @Test
        @DisplayName("CLICK 이벤트 가중치")
        void handle_ClickEvent() {
            // Given
            EventPayload payload = createPayload(1L, "CLICK", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.3);
        }

        @Test
        @DisplayName("BOOKMARK 이벤트 가중치")
        void handle_BookmarkEvent() {
            // Given
            EventPayload payload = createPayload(1L, "BOOKMARK", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 1.0);
        }

        @Test
        @DisplayName("LIKE 이벤트 가중치")
        void handle_LikeEvent() {
            // Given
            EventPayload payload = createPayload(1L, "LIKE", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.6);
        }

        @Test
        @DisplayName("FOLLOW 이벤트 가중치")
        void handle_FollowEvent() {
            // Given
            EventPayload payload = createPayload(1L, "FOLLOW", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.8);
        }

        @Test
        @DisplayName("REVIEW_CREATE 이벤트 가중치")
        void handle_ReviewCreateEvent() {
            // Given
            EventPayload payload = createPayload(1L, "REVIEW_CREATE", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.4);
        }

        @Test
        @DisplayName("REVIEW_UPDATE 이벤트 가중치")
        void handle_ReviewUpdateEvent() {
            // Given
            EventPayload payload = createPayload(1L, "REVIEW_UPDATE", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.4);
        }
    }

    @Nested
    @DisplayName("DWELL 이벤트 가중치 계산")
    class DwellEventWeight {

        @Test
        @DisplayName("DWELL 이벤트 - dwellMs에 비례한 가중치")
        void handle_DwellEvent_CalculatesWeightBasedOnDwellMs() {
            // Given
            Map<String, Object> metadata = Map.of("dwellMs", 500.0);
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // 500 * 0.001 = 0.5
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.5);
        }

        @Test
        @DisplayName("DWELL 이벤트 - 최대값 제한 적용")
        void handle_DwellEvent_AppliesMaxCap() {
            // Given
            Map<String, Object> metadata = Map.of("dwellMs", 5000.0);
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // 5000 * 0.001 = 5.0 -> capped at 1.5
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 1.5);
        }

        @Test
        @DisplayName("DWELL 이벤트 - dwellMs가 없으면 가중치 0")
        void handle_DwellEvent_NoDwellMs_ZeroWeight() {
            // Given
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            // null metadata -> dwellMs = 0 -> weight = 0
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("DWELL 이벤트 - dwellMs가 문자열로 전달되어도 파싱")
        void handle_DwellEvent_DwellMsAsString() {
            // Given
            Map<String, Object> metadata = Map.of("dwellMs", "800");
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // 800 * 0.001 = 0.8
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.8);
        }

        @Test
        @DisplayName("DWELL 이벤트 - dwellMs가 정수로 전달되어도 처리")
        void handle_DwellEvent_DwellMsAsInteger() {
            // Given
            Map<String, Object> metadata = Map.of("dwellMs", 1000);
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // 1000 * 0.001 = 1.0
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 1.0);
        }
    }

    @Nested
    @DisplayName("SCROLL 이벤트 가중치 계산")
    class ScrollEventWeight {

        @Test
        @DisplayName("SCROLL 이벤트 - scrollDepthPct에 비례한 가중치")
        void handle_ScrollEvent_CalculatesWeightBasedOnScrollDepth() {
            // Given
            Map<String, Object> metadata = Map.of("scrollDepthPct", 50.0);
            EventPayload payload = createPayload(1L, "SCROLL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // 50 * 0.005 = 0.25
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.25);
        }

        @Test
        @DisplayName("SCROLL 이벤트 - 최대값 제한 적용")
        void handle_ScrollEvent_AppliesMaxCap() {
            // Given
            Map<String, Object> metadata = Map.of("scrollDepthPct", 200.0);
            EventPayload payload = createPayload(1L, "SCROLL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // 200 * 0.005 = 1.0 -> capped at 0.5
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.5);
        }

        @Test
        @DisplayName("SCROLL 이벤트 - scrollDepthPct가 없으면 가중치 0")
        void handle_ScrollEvent_NoScrollDepth_ZeroWeight() {
            // Given
            EventPayload payload = createPayload(1L, "SCROLL", "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }
    }

    @Nested
    @DisplayName("metadata 파싱")
    class MetadataParsing {

        @Test
        @DisplayName("metadata의 숫자 값을 double로 변환")
        void handle_MetadataNumberValue() {
            // Given
            Map<String, Object> metadata = Map.of("dwellMs", 100L);
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 100L, 0.1);
        }

        @Test
        @DisplayName("metadata의 파싱 불가능한 문자열은 0으로 처리")
        void handle_MetadataInvalidStringValue() {
            // Given
            Map<String, Object> metadata = Map.of("dwellMs", "not-a-number");
            EventPayload payload = createPayload(1L, "DWELL", "BOOK", "100", metadata);

            // When
            handler.handle(payload);

            // Then
            // "not-a-number" 파싱 실패 -> 0.0 -> weight = 0
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }

        @Test
        @DisplayName("contentType이 숫자로 저장되어도 문자열로 변환")
        void handle_ContentTypeAsNumber() {
            // Given
            Map<String, Object> metadata = Map.of(
                    "contentType", 123,
                    "contentId", "100"
            );
            EventPayload payload = createPayload(1L, "CLICK", null, null, metadata);

            // When
            handler.handle(payload);

            // Then
            // contentType이 "123"이 되어 알 수 없는 타입 -> 호출 안됨
            verify(cacheService, never()).incrementBookScore(anyLong(), anyLong(), anyDouble());
        }
    }

    @Nested
    @DisplayName("복합 시나리오")
    class ComplexScenarios {

        @Test
        @DisplayName("여러 필드가 metadata로 오버라이드된 경우")
        void handle_MultipleFieldsFromMetadata() {
            // Given
            Map<String, Object> metadata = Map.of(
                    "contentType", "BOOK",
                    "contentId", 300
            );
            // targetType과 targetId는 다른 값을 가지지만 metadata가 우선
            EventPayload payload = createPayload(1L, "BOOKMARK", "REVIEW", "999", metadata);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(1L, 300L, 1.0);
        }

        @ParameterizedTest
        @ValueSource(strings = {"IMPRESSION", "CLICK", "BOOKMARK", "LIKE", "FOLLOW", "REVIEW_CREATE", "REVIEW_UPDATE"})
        @DisplayName("다양한 이벤트 타입에 대해 올바르게 처리")
        void handle_VariousEventTypes(String eventType) {
            // Given
            EventPayload payload = createPayload(1L, eventType, "BOOK", "100", null);

            // When
            handler.handle(payload);

            // Then
            verify(cacheService, times(1)).incrementBookScore(eq(1L), eq(100L), anyDouble());
        }
    }

    // Helper methods
    private EventPayload createPayload(Long userId, String eventType, String targetType, String targetId, Map<String, Object> metadata) {
        return new EventPayload(
                UUID.randomUUID(),
                eventType,
                userId,
                targetType,
                targetId,
                metadata,
                Instant.now(),
                "test",
                1
        );
    }

    private Review createReview(Long reviewId, Long userId, Long bookId) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                Rating.of(5),
                "Test summary",
                "Test content",
                LocalDateTime.now(),
                ReviewVisibility.PUBLIC,
                false,
                0,
                BookGenre.FICTION,
                Collections.emptyList()
        );
    }
}
