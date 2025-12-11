package org.yyubin.application.bookmark.dto;

import java.util.Collections;
import java.util.List;

public record ReviewBookmarkPageResult(
        List<ReviewBookmarkItem> items,
        Long nextCursor
) {
    public ReviewBookmarkPageResult {
        items = items == null ? Collections.emptyList() : List.copyOf(items);
    }
}
