package org.yyubin.application.bookmark.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.bookmark.command.AddBookmarkCommand;
import org.yyubin.application.bookmark.command.RemoveBookmarkCommand;
import org.yyubin.application.bookmark.dto.ReviewBookmarkPageResult;
import org.yyubin.application.bookmark.port.ReviewBookmarkRepository;
import org.yyubin.application.bookmark.query.GetBookmarksQuery;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.book.BookMetadata;
import org.yyubin.domain.bookmark.ReviewBookmark;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewBookmarkService 테스트")
class ReviewBookmarkServiceTest {

    @Mock
    private ReviewBookmarkRepository reviewBookmarkRepository;

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadBookPort loadBookPort;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ReviewBookmarkService reviewBookmarkService;

    private Review testReview;
    private Book testBook;
    private ReviewBookmark testBookmark;

    @BeforeEach
    void setUp() {
        testBook = createTestBook(1L);
        testReview = createTestReview(100L, 1L, 1L);
        testBookmark = new ReviewBookmark(
                1L,
                new UserId(1L),
                ReviewId.of(100L),
                LocalDateTime.now()
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

    private Review createTestReview(Long reviewId, Long userId, Long bookId) {
        return createTestReview(reviewId, userId, bookId, false, ReviewVisibility.PUBLIC);
    }

    private Review createTestReview(Long reviewId, Long userId, Long bookId, boolean deleted, ReviewVisibility visibility) {
        return Review.of(
                ReviewId.of(reviewId),
                new UserId(userId),
                BookId.of(bookId),
                org.yyubin.domain.review.Rating.of(5),
                "Great book!",
                LocalDateTime.now(),
                visibility,
                deleted,
                0L,
                org.yyubin.domain.review.BookGenre.ESSAY,
                List.of()
        );
    }

    @Test
    @DisplayName("북마크 추가 성공 - 새로운 북마크")
    void add_Success_NewBookmark() {
        // Given
        AddBookmarkCommand command = new AddBookmarkCommand(1L, 100L);

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(reviewBookmarkRepository.findByUserAndReview(any(UserId.class), any(ReviewId.class)))
                .thenReturn(Optional.empty());
        when(reviewBookmarkRepository.save(any(ReviewBookmark.class)))
                .thenReturn(testBookmark);

        // When
        ReviewBookmark result = reviewBookmarkService.add(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId().value()).isEqualTo(1L);
        assertThat(result.reviewId().getValue()).isEqualTo(100L);

        verify(loadReviewPort, atLeastOnce()).loadById(100L);
        verify(reviewBookmarkRepository).findByUserAndReview(any(UserId.class), any(ReviewId.class));
        verify(reviewBookmarkRepository).save(any(ReviewBookmark.class));
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("북마크 추가 성공 - 이미 존재하는 북마크")
    void add_Success_ExistingBookmark() {
        // Given
        AddBookmarkCommand command = new AddBookmarkCommand(1L, 100L);

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(reviewBookmarkRepository.findByUserAndReview(any(UserId.class), any(ReviewId.class)))
                .thenReturn(Optional.of(testBookmark));

        // When
        ReviewBookmark result = reviewBookmarkService.add(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testBookmark);

        verify(loadReviewPort, atLeastOnce()).loadById(100L);
        verify(reviewBookmarkRepository).findByUserAndReview(any(UserId.class), any(ReviewId.class));
        verify(reviewBookmarkRepository, never()).save(any());
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("북마크 추가 실패 - 삭제된 리뷰")
    void add_Fail_DeletedReview() {
        // Given
        AddBookmarkCommand command = new AddBookmarkCommand(1L, 100L);
        Review deletedReview = createTestReview(100L, 1L, 1L, true, ReviewVisibility.PUBLIC);

        when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);

        // When & Then
        assertThatThrownBy(() -> reviewBookmarkService.add(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(reviewBookmarkRepository, never()).save(any());
    }

    @Test
    @DisplayName("북마크 제거 성공")
    void remove_Success() {
        // Given
        RemoveBookmarkCommand command = new RemoveBookmarkCommand(1L, 100L);

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        doNothing().when(reviewBookmarkRepository).delete(any(UserId.class), any(ReviewId.class));

        // When
        reviewBookmarkService.remove(command);

        // Then
        verify(reviewBookmarkRepository).delete(any(UserId.class), any(ReviewId.class));
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("북마크 조회 성공 - 다음 페이지 있음")
    void query_Success_WithNextPage() {
        // Given
        GetBookmarksQuery query = new GetBookmarksQuery(1L, null, 2);

        ReviewBookmark bookmark1 = new ReviewBookmark(1L, new UserId(1L), ReviewId.of(100L), LocalDateTime.now());
        ReviewBookmark bookmark2 = new ReviewBookmark(2L, new UserId(1L), ReviewId.of(101L), LocalDateTime.now());
        ReviewBookmark bookmark3 = new ReviewBookmark(3L, new UserId(1L), ReviewId.of(102L), LocalDateTime.now());

        Review review1 = createTestReview(100L, 1L, 1L);
        Review review2 = createTestReview(101L, 1L, 2L);

        when(reviewBookmarkRepository.findByUserAfterCursor(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of(bookmark1, bookmark2, bookmark3));
        when(loadReviewPort.loadById(100L)).thenReturn(review1);
        when(loadReviewPort.loadById(101L)).thenReturn(review2);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));
        when(loadBookPort.loadById(2L)).thenReturn(Optional.of(testBook));

        // When
        ReviewBookmarkPageResult result = reviewBookmarkService.query(query);

        // Then
        assertThat(result.items()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(3L);

        verify(reviewBookmarkRepository).findByUserAfterCursor(any(UserId.class), any(), eq(3));
    }

    @Test
    @DisplayName("북마크 조회 성공 - 다음 페이지 없음")
    void query_Success_NoNextPage() {
        // Given
        GetBookmarksQuery query = new GetBookmarksQuery(1L, null, 10);

        ReviewBookmark bookmark1 = new ReviewBookmark(1L, new UserId(1L), ReviewId.of(100L), LocalDateTime.now());
        Review review1 = createTestReview(100L, 1L, 1L);

        when(reviewBookmarkRepository.findByUserAfterCursor(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of(bookmark1));
        when(loadReviewPort.loadById(100L)).thenReturn(review1);
        when(loadBookPort.loadById(1L)).thenReturn(Optional.of(testBook));

        // When
        ReviewBookmarkPageResult result = reviewBookmarkService.query(query);

        // Then
        assertThat(result.items()).hasSize(1);
        assertThat(result.nextCursor()).isNull();

        verify(reviewBookmarkRepository).findByUserAfterCursor(any(UserId.class), any(), eq(11));
    }

    @Test
    @DisplayName("북마크 조회 성공 - 빈 결과")
    void query_Success_EmptyResult() {
        // Given
        GetBookmarksQuery query = new GetBookmarksQuery(1L, null, 10);

        when(reviewBookmarkRepository.findByUserAfterCursor(any(UserId.class), any(), anyInt()))
                .thenReturn(List.of());

        // When
        ReviewBookmarkPageResult result = reviewBookmarkService.query(query);

        // Then
        assertThat(result.items()).isEmpty();
        assertThat(result.nextCursor()).isNull();

        verify(reviewBookmarkRepository).findByUserAfterCursor(any(UserId.class), any(), eq(11));
    }
}
