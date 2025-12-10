package org.yyubin.application.review.command;

public record CreateCommentCommand(
        Long reviewId,
        Long userId,
        String content,
        Long parentCommentId
) {
}
