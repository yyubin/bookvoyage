package org.yyubin.application.review.command;

public record DeleteReactionCommand(
        Long reviewId,
        Long userId
) {
}
