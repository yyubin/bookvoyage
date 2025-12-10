package org.yyubin.application.review.command;

public record DeleteReviewCommand(
        Long reviewId,
        Long userId
) {
}
