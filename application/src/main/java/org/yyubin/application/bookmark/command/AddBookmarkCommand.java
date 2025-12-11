package org.yyubin.application.bookmark.command;

public record AddBookmarkCommand(
        Long userId,
        Long reviewId
) {
}
