package org.yyubin.application.feed.query;

public record GetFeedQuery(
        Long userId,
        Long cursorScore,
        int size
) {
}
