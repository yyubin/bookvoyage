package org.yyubin.api.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.review.dto.CommentPageResponse;
import org.yyubin.api.review.dto.CommentResponse;
import org.yyubin.api.review.dto.CreateCommentRequest;
import org.yyubin.api.review.dto.UpdateCommentRequest;
import org.yyubin.application.review.CreateCommentUseCase;
import org.yyubin.application.review.DeleteCommentUseCase;
import org.yyubin.application.review.GetCommentsUseCase;
import org.yyubin.application.review.GetRepliesUseCase;
import org.yyubin.application.review.dto.PagedCommentResult;
import org.yyubin.application.review.UpdateCommentUseCase;
import org.yyubin.application.review.command.CreateCommentCommand;
import org.yyubin.application.review.command.DeleteCommentCommand;
import org.yyubin.application.review.command.UpdateCommentCommand;
import org.yyubin.application.review.query.GetCommentsQuery;
import org.yyubin.application.review.query.GetRepliesQuery;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewCommentController {

    private final CreateCommentUseCase createCommentUseCase;
    private final UpdateCommentUseCase updateCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;
    private final GetCommentsUseCase getCommentsUseCase;
    private final GetRepliesUseCase getRepliesUseCase;

    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<CommentPageResponse> getComments(
            @PathVariable Long reviewId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Object principal
    ) {
        Long viewerId = resolveViewerId(principal);
        PagedCommentResult result = getCommentsUseCase.query(new GetCommentsQuery(reviewId, viewerId, cursor, size));
        return ResponseEntity.ok(CommentPageResponse.from(result));
    }

    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CreateCommentCommand command = new CreateCommentCommand(
                reviewId,
                userId,
                request.content(),
                request.parentCommentId()
        );

        return ResponseEntity.ok(CommentResponse.from(createCommentUseCase.execute(command)));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UpdateCommentCommand command = new UpdateCommentCommand(commentId, userId, request.content());
        return ResponseEntity.ok(CommentResponse.from(updateCommentUseCase.execute(command)));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        deleteCommentUseCase.execute(new DeleteCommentCommand(commentId, userId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentPageResponse> getReplies(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Object principal
    ) {
        Long viewerId = resolveViewerId(principal);
        PagedCommentResult result = getRepliesUseCase.query(new GetRepliesQuery(commentId, viewerId, cursor, size));
        return ResponseEntity.ok(CommentPageResponse.from(result));
    }

    private Long resolveViewerId(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
