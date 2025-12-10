package org.yyubin.api.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.review.ReviewCommentResult;

public record CommentResponse(
        Long commentId,
        Long reviewId,
        Long userId,
        String content,
        Long parentCommentId,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        List<MentionResponse> mentions
) {

    public static CommentResponse from(ReviewCommentResult result) {
        return new CommentResponse(
                result.commentId(),
                result.reviewId(),
                result.userId(),
                result.content(),
                result.parentCommentId(),
                result.createdAt(),
                result.editedAt(),
                result.mentions().stream().map(MentionResponse::from).toList()
        );
    }
}
