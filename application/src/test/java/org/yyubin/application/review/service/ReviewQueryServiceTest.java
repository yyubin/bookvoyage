package org.yyubin.application.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.dto.PagedReviewResult;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewViewMetricPort;
import org.yyubin.application.review.query.GetReviewQuery;
import org.yyubin.application.review.query.GetUserReviewsQuery;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.*;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewQueryService 테스트")
class ReviewQueryServiceTest {

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadBookPort loadBookPort;

    @Mock
    private LoadKeywordsUseCase loadKeywordsUseCase;

    @Mock
    private LoadHighlightsUseCase loadHighlightsUseCase;

    @Mock
    private ReviewViewMetricPort reviewViewMetricPort;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private HighlightNormalizer highlightNormalizer;

    @InjectMocks
    private ReviewQueryService reviewQueryService;

    private Review testReview;
    private Book testBook;

    @BeforeEach
    void setUp() {
        testReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PUBLIC);
        testBook = createTestBook(1L);
    }

    private Review createTestReview(Long reviewId, Long userId, Long bookId, boolean deleted, ReviewVisibility visibility) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                Rating.of(5),
                "Great book!",
                LocalDateTime.now(),
                visibility,
                deleted,
                100L,
                BookGenre.ESSAY,
                List.of()
        );
    }

    private Book createTestBook(Long bookId) {
        Book book = Book.create(
                "Clean Code",
                List.of("Robert C. Martin"),
                "0132350882",
                "9780132350884",
                "http://example.com/cover.jpg",
                "Prentice Hall",
                "2008-08-01",
                "A Handbook of Agile Software Craftsmanship",
                "en",
                464,
                "google-volume-id-123"
        );
        return Book.of(BookId.of(bookId), book.getMetadata());
    }

    @Test
    @DisplayName("리뷰 조회 성공 - 공개 리뷰")
    void query_Success_PublicReview() {
        // Given
        GetReviewQuery query = new GetReviewQuery(100L, 2L);

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any(ReviewId.class))).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any(ReviewId.class))).thenReturn(List.of());
        when(reviewViewMetricPort.incrementAndGet(100L, 2L)).thenReturn(101L);
        when(reviewViewMetricPort.getCachedCount(100L)).thenReturn(Optional.of(101L));

        // When
        ReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.reviewId()).isEqualTo(100L);

        verify(loadReviewPort).loadById(100L);
        verify(loadBookPort).loadById(1L);
        verify(reviewViewMetricPort).incrementAndGet(100L, 2L);
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("리뷰 조회 성공 - 캐시된 조회수 사용")
    void query_Success_UseCachedViewCount() {
        // Given
        GetReviewQuery query = new GetReviewQuery(100L, null);

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any(ReviewId.class))).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any(ReviewId.class))).thenReturn(List.of());
        when(reviewViewMetricPort.incrementAndGet(100L, null)).thenReturn(101L);
        when(reviewViewMetricPort.getCachedCount(100L)).thenReturn(Optional.of(150L));

        // When
        ReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result).isNotNull();
        verify(reviewViewMetricPort).getCachedCount(100L);
    }

    @Test
    @DisplayName("리뷰 조회 성공 - 비공개 리뷰, 작성자가 조회")
    void query_Success_PrivateReview_ByAuthor() {
        // Given
        Review privateReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PRIVATE);
        GetReviewQuery query = new GetReviewQuery(100L, 1L);

        when(loadReviewPort.loadById(100L)).thenReturn(privateReview);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any(ReviewId.class))).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any(ReviewId.class))).thenReturn(List.of());
        when(reviewViewMetricPort.incrementAndGet(100L, 1L)).thenReturn(101L);
        when(reviewViewMetricPort.getCachedCount(100L)).thenReturn(Optional.of(101L));

        // When
        ReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result).isNotNull();
        verify(loadReviewPort).loadById(100L);
    }

    @Test
    @DisplayName("리뷰 조회 실패 - 삭제된 리뷰")
    void query_Fail_DeletedReview() {
        // Given
        Review deletedReview = createTestReview(100L, 1L, 1L, true, ReviewVisibility.PUBLIC);
        GetReviewQuery query = new GetReviewQuery(100L, 2L);

        when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);

        // When & Then
        assertThatThrownBy(() -> reviewQueryService.query(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(loadBookPort, never()).loadById(anyLong());
    }

    @Test
    @DisplayName("리뷰 조회 실패 - 비공개 리뷰, 작성자가 아닌 사람이 조회")
    void query_Fail_PrivateReview_ByNonAuthor() {
        // Given
        Review privateReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PRIVATE);
        GetReviewQuery query = new GetReviewQuery(100L, 2L);

        when(loadReviewPort.loadById(100L)).thenReturn(privateReview);

        // When & Then
        assertThatThrownBy(() -> reviewQueryService.query(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(loadBookPort, never()).loadById(anyLong());
    }

    @Test
    @DisplayName("리뷰 조회 실패 - 비공개 리뷰, 비로그인 사용자가 조회")
    void query_Fail_PrivateReview_Anonymous() {
        // Given
        Review privateReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PRIVATE);
        GetReviewQuery query = new GetReviewQuery(100L, null);

        when(loadReviewPort.loadById(100L)).thenReturn(privateReview);

        // When & Then
        assertThatThrownBy(() -> reviewQueryService.query(query))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(loadBookPort, never()).loadById(anyLong());
    }

    @Test
    @DisplayName("사용자 리뷰 목록 조회 성공 - 다음 페이지 있음")
    void queryUserReviews_Success_WithNextPage() {
        // Given
        GetUserReviewsQuery query = new GetUserReviewsQuery(1L, null, null, 2);

        Review review1 = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PUBLIC);
        Review review2 = createTestReview(101L, 1L, 2L, false, ReviewVisibility.PUBLIC);
        Review review3 = createTestReview(102L, 1L, 3L, false, ReviewVisibility.PUBLIC);

        Book book2 = createTestBook(2L);
        Book book3 = createTestBook(3L);

        when(loadReviewPort.loadByUserId(1L, null, null, 3))
                .thenReturn(List.of(review1, review2, review3));
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadBookPort.loadById(2L)).thenReturn(Optional.of(book2));
        when(loadKeywordsUseCase.loadKeywords(any(ReviewId.class))).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any(ReviewId.class))).thenReturn(List.of());
        when(reviewViewMetricPort.getCachedCount(anyLong())).thenReturn(Optional.empty());

        // When
        PagedReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result.reviews()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(102L);

        verify(loadReviewPort).loadByUserId(1L, null, null, 3);
    }

    @Test
    @DisplayName("사용자 리뷰 목록 조회 성공 - 다음 페이지 없음")
    void queryUserReviews_Success_NoNextPage() {
        // Given
        GetUserReviewsQuery query = new GetUserReviewsQuery(1L, null, null, 10);

        Review review1 = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PUBLIC);

        when(loadReviewPort.loadByUserId(1L, null, null, 11))
                .thenReturn(List.of(review1));
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any(ReviewId.class))).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any(ReviewId.class))).thenReturn(List.of());
        when(reviewViewMetricPort.getCachedCount(anyLong())).thenReturn(Optional.empty());

        // When
        PagedReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.nextCursor()).isNull();

        verify(loadReviewPort).loadByUserId(1L, null, null, 11);
    }

    @Test
    @DisplayName("사용자 리뷰 목록 조회 성공 - 빈 결과")
    void queryUserReviews_Success_EmptyResult() {
        // Given
        GetUserReviewsQuery query = new GetUserReviewsQuery(1L, 2L, null, 10);

        when(loadReviewPort.loadByUserId(1L, 2L, null, 11))
                .thenReturn(List.of());

        // When
        PagedReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result.reviews()).isEmpty();
        assertThat(result.nextCursor()).isNull();

        verify(loadReviewPort).loadByUserId(1L, 2L, null, 11);
    }

    @Test
    @DisplayName("사용자 리뷰 목록 조회 성공 - 커서 기반 페이지네이션")
    void queryUserReviews_Success_WithCursor() {
        // Given
        GetUserReviewsQuery query = new GetUserReviewsQuery(1L, null, 99L, 5);

        Review review1 = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PUBLIC);

        when(loadReviewPort.loadByUserId(1L, null, 99L, 6))
                .thenReturn(List.of(review1));
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any(ReviewId.class))).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any(ReviewId.class))).thenReturn(List.of());
        when(reviewViewMetricPort.getCachedCount(anyLong())).thenReturn(Optional.empty());

        // When
        PagedReviewResult result = reviewQueryService.query(query);

        // Then
        assertThat(result.reviews()).hasSize(1);
        assertThat(result.nextCursor()).isNull();

        verify(loadReviewPort).loadByUserId(1L, null, 99L, 6);
    }
}
