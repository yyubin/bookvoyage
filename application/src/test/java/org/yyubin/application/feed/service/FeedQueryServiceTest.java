package org.yyubin.application.feed.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.feed.dto.FeedPageResult;
import org.yyubin.application.feed.port.FeedItemPort;
import org.yyubin.application.feed.query.GetFeedQuery;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedQueryService 테스트")
class FeedQueryServiceTest {

    @Mock
    private FeedItemPort feedItemPort;

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadBookPort loadBookPort;

    @Mock
    private LoadKeywordsUseCase loadKeywordsUseCase;

    @Mock
    private LoadHighlightsUseCase loadHighlightsUseCase;

    @InjectMocks
    private FeedQueryService feedQueryService;

    private Book testBook;
    private Review testReview;
    private FeedItem testFeedItem;

    @BeforeEach
    void setUp() {
        testBook = createTestBook(1L);
        testReview = createTestReview(100L, 1L, 1L);
        testFeedItem = FeedItem.of(1L, new UserId(1L), ReviewId.of(100L), LocalDateTime.now());
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

    private Review createTestReview(Long reviewId, Long userId, Long bookId) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                org.yyubin.domain.review.Rating.of(5),
                "Great book!",
                LocalDateTime.now(),
                ReviewVisibility.PUBLIC,
                false,
                0L,
                org.yyubin.domain.review.BookGenre.ESSAY,
                List.of()
        );
    }

    @Test
    @DisplayName("피드 조회 성공 - 다음 페이지 있음")
    void query_Success_WithNextPage() {
        // Given
        GetFeedQuery query = new GetFeedQuery(1L, null, 2);

        FeedItem feedItem1 = FeedItem.of(1L, new UserId(1L), ReviewId.of(100L), LocalDateTime.now());
        FeedItem feedItem2 = FeedItem.of(2L, new UserId(1L), ReviewId.of(101L), LocalDateTime.now());
        FeedItem feedItem3 = FeedItem.of(3L, new UserId(1L), ReviewId.of(102L), LocalDateTime.now());

        Review review1 = createTestReview(100L, 2L, 1L);
        Review review2 = createTestReview(101L, 3L, 1L);

        when(feedItemPort.loadFeed(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of(feedItem1, feedItem2, feedItem3));
        when(loadReviewPort.loadById(100L)).thenReturn(review1);
        when(loadReviewPort.loadById(101L)).thenReturn(review2);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

        // When
        FeedPageResult result = feedQueryService.query(query);

        // Then
        assertThat(result.items()).hasSize(2);
        assertThat(result.nextCursorEpochMillis()).isNotNull();

        verify(feedItemPort).loadFeed(any(UserId.class), any(), anyInt());
    }

    @Test
    @DisplayName("피드 조회 성공 - 다음 페이지 없음")
    void query_Success_NoNextPage() {
        // Given
        GetFeedQuery query = new GetFeedQuery(1L, null, 10);

        FeedItem feedItem1 = FeedItem.of(1L, new UserId(1L), ReviewId.of(100L), LocalDateTime.now());

        when(feedItemPort.loadFeed(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of(feedItem1));
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

        // When
        FeedPageResult result = feedQueryService.query(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.nextCursorEpochMillis()).isNull();

        verify(feedItemPort).loadFeed(any(UserId.class), any(), anyInt());
    }

    @Test
    @DisplayName("피드 조회 성공 - 빈 결과")
    void query_Success_EmptyResult() {
        // Given
        GetFeedQuery query = new GetFeedQuery(1L, null, 10);

        when(feedItemPort.loadFeed(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of());

        // When
        FeedPageResult result = feedQueryService.query(query);

        // Then
        assertThat(result.items()).isEmpty();
        assertThat(result.nextCursorEpochMillis()).isNull();

        verify(feedItemPort).loadFeed(any(UserId.class), any(), anyInt());
    }

    @Test
    @DisplayName("피드 조회 성공 - 커서 기반 페이지네이션")
    void query_Success_WithCursor() {
        // Given
        Long cursorScore = 1000000L;
        GetFeedQuery query = new GetFeedQuery(1L, cursorScore, 5);

        FeedItem feedItem1 = FeedItem.of(1L, new UserId(1L), ReviewId.of(100L), LocalDateTime.now());

        when(feedItemPort.loadFeed(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of(feedItem1));
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadKeywordsUseCase.loadKeywords(any())).thenReturn(List.of());
        when(loadHighlightsUseCase.loadHighlights(any())).thenReturn(List.of());

        // When
        FeedPageResult result = feedQueryService.query(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.nextCursorEpochMillis()).isNull();

        verify(feedItemPort).loadFeed(any(UserId.class), anyDouble(), anyInt());
    }
}
