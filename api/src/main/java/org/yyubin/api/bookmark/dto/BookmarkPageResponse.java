package org.yyubin.api.bookmark.dto;

import java.util.List;
import org.yyubin.application.bookmark.dto.ReviewBookmarkPageResult;

public record BookmarkPageResponse(
        List<BookmarkItemResponse> items,
        Long nextCursor
) {
    public static BookmarkPageResponse from(ReviewBookmarkPageResult result) {
        return new BookmarkPageResponse(
                result.items().stream().map(BookmarkItemResponse::from).toList(),
                result.nextCursor()
        );
    }
}
