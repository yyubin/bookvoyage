package org.yyubin.application.activity.query;

public record GetActivityFeedQuery(
        Long userId,
        Long cursorEpochMillis,
        Integer size
) {
}
