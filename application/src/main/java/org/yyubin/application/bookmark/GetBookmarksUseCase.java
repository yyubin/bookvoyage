package org.yyubin.application.bookmark;

import org.yyubin.application.bookmark.dto.ReviewBookmarkPageResult;
import org.yyubin.application.bookmark.query.GetBookmarksQuery;

public interface GetBookmarksUseCase {
    ReviewBookmarkPageResult query(GetBookmarksQuery query);
}
