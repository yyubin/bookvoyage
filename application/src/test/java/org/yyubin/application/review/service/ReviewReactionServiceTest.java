package org.yyubin.application.review.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.review.command.DeleteReactionCommand;
import org.yyubin.application.review.command.UpsertReactionCommand;
import org.yyubin.application.review.dto.ReviewReactionResult;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewReactionPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.*;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.review.ReviewReactionId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewReactionService 테스트")
class ReviewReactionServiceTest {

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private ReviewReactionPort reviewReactionPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private NotificationEventUseCase notificationEventUseCase;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ReviewReactionService reviewReactionService;

    private Review testReview;
    private ReviewReaction testReaction;

    @BeforeEach
    void setUp() {
        testReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PUBLIC);
        testReaction = createTestReaction(1L, 100L, 2L);
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
                0L,
                org.yyubin.domain.review.BookGenre.ESSAY,
                List.of()
        );
    }

    private ReviewReaction createTestReaction(Long reactionId, Long reviewId, Long userId) {
        return ReviewReaction.of(
                ReviewReactionId.of(reactionId),
                ReviewId.of(reviewId),
                new UserId(userId),
                "👍",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("리액션 생성 성공 - 새로운 리액션")
    void execute_UpsertReaction_Success_NewReaction() {
        // Given
        UpsertReactionCommand command = new UpsertReactionCommand(100L, 2L, "👍");

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(reviewReactionPort.loadByReviewIdAndUserId(100L, 2L)).thenReturn(Optional.empty());
        when(reviewReactionPort.save(any(ReviewReaction.class))).thenReturn(testReaction);

        // When
        ReviewReactionResult result = reviewReactionService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.reactionId()).isEqualTo(1L);
        assertThat(result.content()).isEqualTo("👍");

        verify(loadUserPort).loadById(any(UserId.class));
        verify(loadReviewPort, atLeastOnce()).loadById(100L);
        verify(reviewReactionPort).loadByReviewIdAndUserId(100L, 2L);
        verify(reviewReactionPort).save(any(ReviewReaction.class));
        verify(notificationEventUseCase).handle(any());
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("리액션 업데이트 성공 - 기존 리액션 변경")
    void execute_UpsertReaction_Success_UpdateExisting() {
        // Given
        UpsertReactionCommand command = new UpsertReactionCommand(100L, 2L, "❤️");

        ReviewReaction existingReaction = ReviewReaction.of(
                ReviewReactionId.of(1L),
                ReviewId.of(100L),
                new UserId(2L),
                "👍",
                LocalDateTime.now().minusDays(1)
        );

        ReviewReaction updatedReaction = ReviewReaction.of(
                ReviewReactionId.of(1L),
                ReviewId.of(100L),
                new UserId(2L),
                "❤️",
                LocalDateTime.now().minusDays(1)
        );

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(reviewReactionPort.loadByReviewIdAndUserId(100L, 2L))
                .thenReturn(Optional.of(existingReaction));
        when(reviewReactionPort.save(any(ReviewReaction.class))).thenReturn(updatedReaction);

        // When
        ReviewReactionResult result = reviewReactionService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("❤️");

        verify(reviewReactionPort).loadByReviewIdAndUserId(100L, 2L);
        verify(reviewReactionPort).save(any(ReviewReaction.class));
    }

    @Test
    @DisplayName("리액션 생성 실패 - 삭제된 리뷰")
    void execute_UpsertReaction_Fail_DeletedReview() {
        // Given
        UpsertReactionCommand command = new UpsertReactionCommand(100L, 2L, "👍");

        Review deletedReview = createTestReview(100L, 1L, 1L, true, ReviewVisibility.PUBLIC);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);

        // When & Then
        assertThatThrownBy(() -> reviewReactionService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(reviewReactionPort, never()).save(any());
    }

    @Test
    @DisplayName("리액션 생성 실패 - 비공개 리뷰에 작성자 아닌 사람이 리액션")
    void execute_UpsertReaction_Fail_PrivateReview() {
        // Given
        UpsertReactionCommand command = new UpsertReactionCommand(100L, 2L, "👍");

        Review privateReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PRIVATE);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(privateReview);

        // When & Then
        assertThatThrownBy(() -> reviewReactionService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(reviewReactionPort, never()).save(any());
    }

    @Test
    @DisplayName("자신의 리뷰에 리액션 - 알림 발송 안함")
    void execute_UpsertReaction_Success_OwnReview_NoNotification() {
        // Given
        UpsertReactionCommand command = new UpsertReactionCommand(100L, 1L, "👍"); // same user as review author

        ReviewReaction ownReaction = createTestReaction(1L, 100L, 1L);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(reviewReactionPort.loadByReviewIdAndUserId(100L, 1L)).thenReturn(Optional.empty());
        when(reviewReactionPort.save(any(ReviewReaction.class))).thenReturn(ownReaction);

        // When
        ReviewReactionResult result = reviewReactionService.execute(command);

        // Then
        assertThat(result).isNotNull();

        verify(notificationEventUseCase, never()).handle(any());
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("리액션 삭제 성공")
    void execute_DeleteReaction_Success() {
        // Given
        DeleteReactionCommand command = new DeleteReactionCommand(100L, 2L);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        doNothing().when(reviewReactionPort).delete(anyLong(), anyLong());

        // When
        reviewReactionService.execute(command);

        // Then
        verify(loadUserPort).loadById(any(UserId.class));
        verify(loadReviewPort, atLeastOnce()).loadById(100L);
        verify(reviewReactionPort).delete(100L, 2L);
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("리액션 삭제 실패 - 삭제된 리뷰")
    void execute_DeleteReaction_Fail_DeletedReview() {
        // Given
        DeleteReactionCommand command = new DeleteReactionCommand(100L, 2L);

        Review deletedReview = createTestReview(100L, 1L, 1L, true, ReviewVisibility.PUBLIC);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);

        // When & Then
        assertThatThrownBy(() -> reviewReactionService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(reviewReactionPort, never()).delete(anyLong(), anyLong());
    }
}
