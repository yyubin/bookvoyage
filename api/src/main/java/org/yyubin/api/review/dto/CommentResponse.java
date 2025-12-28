package org.yyubin.api.review.dto;

import java.util.List;
import org.yyubin.api.common.TimeFormatter;
import org.yyubin.application.review.dto.ReviewCommentResult;

public record CommentResponse(
        Long commentId,
        Long reviewId,
        Long userId,
        String authorNickname,
        String content,
        Long parentCommentId,
        String createdAt,
        String editedAt,
        List<MentionResponse> mentions,
        long replyCount
) {

    public static CommentResponse from(ReviewCommentResult result) {
        return new CommentResponse(
                result.commentId(),
                result.reviewId(),
                result.userId(),
                result.authorNickname(),
                result.content(),
                result.parentCommentId(),
                TimeFormatter.formatRelativeTime(result.createdAt()),
                result.editedAt() != null ? TimeFormatter.formatRelativeTime(result.editedAt()) : null,
                result.mentions().stream().map(MentionResponse::from).toList(),
                result.replyCount()
        );
    }
}
