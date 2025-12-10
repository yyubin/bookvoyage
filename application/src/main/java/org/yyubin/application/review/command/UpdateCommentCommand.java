package org.yyubin.application.review.command;

public record UpdateCommentCommand(
        Long commentId,
        Long userId,
        String content
) {
}
