package org.yyubin.application.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.domain.review.Mention;
import org.yyubin.domain.review.ReviewComment;
import org.yyubin.domain.review.ReviewCommentId;

public record ReviewCommentResult(
        Long commentId,
        Long reviewId,
        Long userId,
        String authorNickname,
        String content,
        Long parentCommentId,
        LocalDateTime createdAt,
        LocalDateTime editedAt,
        List<Mention> mentions
) {

    public static ReviewCommentResult from(ReviewComment comment, org.yyubin.domain.user.User author) {
        ReviewCommentId parent = comment.getParentId();
        return new ReviewCommentResult(
                comment.getId().getValue(),
                comment.getReviewId().getValue(),
                comment.getUserId().value(),
                author.nickname(),
                comment.getContent(),
                parent != null ? parent.getValue() : null,
                comment.getCreatedAt(),
                comment.getEditedAt(),
                comment.getMentions()
        );
    }
}
