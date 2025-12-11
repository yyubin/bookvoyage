package org.yyubin.application.bookmark.query;

public record GetBookmarksQuery(
        Long userId,
        Long cursorId,
        int size
) {
}
