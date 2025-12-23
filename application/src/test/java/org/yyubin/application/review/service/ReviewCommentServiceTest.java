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
import org.yyubin.application.review.command.CreateCommentCommand;
import org.yyubin.application.review.command.DeleteCommentCommand;
import org.yyubin.application.review.command.UpdateCommentCommand;
import org.yyubin.application.review.dto.PagedCommentResult;
import org.yyubin.application.review.dto.ReviewCommentResult;
import org.yyubin.application.review.port.LoadReviewCommentPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.SaveReviewCommentPort;
import org.yyubin.application.review.query.GetCommentsQuery;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.*;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewCommentService 테스트")
class ReviewCommentServiceTest {

    @Mock
    private LoadReviewPort loadReviewPort;

    @Mock
    private LoadReviewCommentPort loadReviewCommentPort;

    @Mock
    private SaveReviewCommentPort saveReviewCommentPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private MentionParser mentionParser;

    @Mock
    private NotificationEventUseCase notificationEventUseCase;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ReviewCommentService reviewCommentService;

    private Review testReview;
    private ReviewComment testComment;

    @BeforeEach
    void setUp() {
        testReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PUBLIC);
        testComment = createTestComment(1L, 100L, 2L);
        lenient().when(mentionParser.parse(anyString())).thenReturn(List.of());
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

    private ReviewComment createTestComment(Long commentId, Long reviewId, Long userId) {
        return ReviewComment.of(
                ReviewCommentId.of(commentId),
                ReviewId.of(reviewId),
                new UserId(userId),
                "Nice review!",
                null,
                LocalDateTime.now(),
                null,
                false,
                List.of()
        );
    }

    @Test
    @DisplayName("댓글 생성 성공")
    void execute_CreateComment_Success() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(100L, 2L, "Nice review!", null);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(saveReviewCommentPort.save(any(ReviewComment.class))).thenReturn(testComment);

        // When
        ReviewCommentResult result = reviewCommentService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.commentId()).isEqualTo(1L);
        assertThat(result.content()).isEqualTo("Nice review!");

        verify(loadUserPort).loadById(any(UserId.class));
        verify(loadReviewPort).loadById(100L);
        verify(saveReviewCommentPort).save(any(ReviewComment.class));
        verify(notificationEventUseCase).handle(any());
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 삭제된 리뷰")
    void execute_CreateComment_Fail_DeletedReview() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(100L, 2L, "Nice review!", null);

        Review deletedReview = createTestReview(100L, 1L, 1L, true, ReviewVisibility.PUBLIC);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(deletedReview);

        // When & Then
        assertThatThrownBy(() -> reviewCommentService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(saveReviewCommentPort, never()).save(any());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 비공개 리뷰에 작성자 아닌 사람이 댓글")
    void execute_CreateComment_Fail_PrivateReview() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(100L, 2L, "Nice review!", null);

        Review privateReview = createTestReview(100L, 1L, 1L, false, ReviewVisibility.PRIVATE);

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(privateReview);

        // When & Then
        assertThatThrownBy(() -> reviewCommentService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Review not found");

        verify(loadReviewPort).loadById(100L);
        verify(saveReviewCommentPort, never()).save(any());
    }

    @Test
    @DisplayName("대댓글 생성 성공")
    void execute_CreateReply_Success() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(100L, 3L, "Reply to comment", 1L);

        ReviewComment parentComment = ReviewComment.of(
                ReviewCommentId.of(1L),
                ReviewId.of(100L),
                new UserId(2L),
                "Parent comment",
                null,
                LocalDateTime.now(),
                null,
                false,
                List.of()
        );

        ReviewComment savedReply = ReviewComment.of(
                ReviewCommentId.of(2L),
                ReviewId.of(100L),
                new UserId(3L),
                "Reply to comment",
                ReviewCommentId.of(1L),
                LocalDateTime.now(),
                null,
                false,
                List.of()
        );

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadReviewCommentPort.loadById(1L)).thenReturn(parentComment);
        when(saveReviewCommentPort.save(any(ReviewComment.class))).thenReturn(savedReply);

        // When
        ReviewCommentResult result = reviewCommentService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.commentId()).isEqualTo(2L);
        assertThat(result.content()).isEqualTo("Reply to comment");

        verify(loadReviewCommentPort).loadById(1L);
        verify(saveReviewCommentPort).save(any(ReviewComment.class));
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 다른 리뷰의 댓글에 답글")
    void execute_CreateReply_Fail_DifferentReview() {
        // Given
        CreateCommentCommand command = new CreateCommentCommand(100L, 3L, "Reply to comment", 1L);

        ReviewComment differentReviewComment = ReviewComment.of(
                ReviewCommentId.of(1L),
                ReviewId.of(200L), // different review
                new UserId(2L),
                "Parent comment",
                null,
                LocalDateTime.now(),
                null,
                false,
                List.of()
        );

        when(loadUserPort.loadById(any(UserId.class))).thenReturn(null);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadReviewCommentPort.loadById(1L)).thenReturn(differentReviewComment);

        // When & Then
        assertThatThrownBy(() -> reviewCommentService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("different review");

        verify(saveReviewCommentPort, never()).save(any());
    }

    @Test
    @DisplayName("댓글 조회 성공 - 다음 페이지 있음")
    void query_Success_WithNextPage() {
        // Given
        GetCommentsQuery query = new GetCommentsQuery(100L, null, null, 2);

        ReviewComment comment1 = ReviewComment.of(ReviewCommentId.of(1L), ReviewId.of(100L),
                new UserId(2L), "Comment 1", null, LocalDateTime.now(), null, false, List.of());
        ReviewComment comment2 = ReviewComment.of(ReviewCommentId.of(2L), ReviewId.of(100L),
                new UserId(3L), "Comment 2", null, LocalDateTime.now(), null, false, List.of());
        ReviewComment comment3 = ReviewComment.of(ReviewCommentId.of(3L), ReviewId.of(100L),
                new UserId(4L), "Comment 3", null, LocalDateTime.now(), null, false, List.of());

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadReviewCommentPort.loadByReviewId(anyLong(), any(), anyInt()))
                .thenReturn(List.of(comment1, comment2, comment3));

        // When
        PagedCommentResult result = reviewCommentService.query(query);

        // Then
        assertThat(result.comments()).hasSize(2);
        assertThat(result.nextCursor()).isEqualTo(3L);

        verify(loadReviewPort).loadById(100L);
        verify(loadReviewCommentPort).loadByReviewId(100L, null, 3);
    }

    @Test
    @DisplayName("댓글 조회 성공 - 빈 결과")
    void query_Success_EmptyResult() {
        // Given
        GetCommentsQuery query = new GetCommentsQuery(100L, null, null, 10);

        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(loadReviewCommentPort.loadByReviewId(anyLong(), any(), anyInt()))
                .thenReturn(List.of());

        // When
        PagedCommentResult result = reviewCommentService.query(query);

        // Then
        assertThat(result.comments()).isEmpty();
        assertThat(result.nextCursor()).isNull();

        verify(loadReviewPort).loadById(100L);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void execute_UpdateComment_Success() {
        // Given
        UpdateCommentCommand command = new UpdateCommentCommand(1L, 2L, "Updated content");

        ReviewComment updated = ReviewComment.of(
                ReviewCommentId.of(1L),
                ReviewId.of(100L),
                new UserId(2L),
                "Updated content",
                null,
                LocalDateTime.now(),
                LocalDateTime.now(),
                false,
                List.of()
        );

        when(loadReviewCommentPort.loadById(1L)).thenReturn(testComment);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(saveReviewCommentPort.save(any(ReviewComment.class))).thenReturn(updated);

        // When
        ReviewCommentResult result = reviewCommentService.execute(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Updated content");

        verify(loadReviewCommentPort).loadById(1L);
        verify(saveReviewCommentPort).save(any(ReviewComment.class));
    }

    @Test
    @DisplayName("댓글 수정 실패 - 작성자가 아님")
    void execute_UpdateComment_Fail_NotAuthor() {
        // Given
        UpdateCommentCommand command = new UpdateCommentCommand(1L, 999L, "Updated content");

        when(loadReviewCommentPort.loadById(1L)).thenReturn(testComment);

        // When & Then
        assertThatThrownBy(() -> reviewCommentService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the author");

        verify(saveReviewCommentPort, never()).save(any());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void execute_DeleteComment_Success() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(1L, 2L);

        ReviewComment deleted = ReviewComment.of(
                ReviewCommentId.of(1L),
                ReviewId.of(100L),
                new UserId(2L),
                "Nice review!",
                null,
                LocalDateTime.now(),
                null,
                true,
                List.of()
        );

        when(loadReviewCommentPort.loadById(1L)).thenReturn(testComment);
        when(loadReviewPort.loadById(100L)).thenReturn(testReview);
        when(saveReviewCommentPort.save(any(ReviewComment.class))).thenReturn(deleted);

        // When
        reviewCommentService.execute(command);

        // Then
        verify(loadReviewCommentPort).loadById(1L);
        verify(loadReviewPort).loadById(100L);
        verify(saveReviewCommentPort).save(any(ReviewComment.class));
        verify(eventPublisher).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 작성자가 아님")
    void execute_DeleteComment_Fail_NotAuthor() {
        // Given
        DeleteCommentCommand command = new DeleteCommentCommand(1L, 999L);

        when(loadReviewCommentPort.loadById(1L)).thenReturn(testComment);

        // When & Then
        assertThatThrownBy(() -> reviewCommentService.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not the author");

        verify(saveReviewCommentPort, never()).save(any());
    }
}
