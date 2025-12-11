package org.yyubin.application.bookmark.command;

public record RemoveBookmarkCommand(
        Long userId,
        Long reviewId
) {
}
