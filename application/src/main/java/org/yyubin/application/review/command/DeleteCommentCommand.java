package org.yyubin.application.review.command;

public record DeleteCommentCommand(
        Long commentId,
        Long userId
) {
}
