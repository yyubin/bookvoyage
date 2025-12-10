package org.yyubin.application.review.command;

public record UpsertReactionCommand(
        Long reviewId,
        Long userId,
        String content
) {
}
