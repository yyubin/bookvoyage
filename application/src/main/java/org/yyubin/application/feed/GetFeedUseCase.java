package org.yyubin.application.feed;

import org.yyubin.application.feed.dto.FeedPageResult;
import org.yyubin.application.feed.query.GetFeedQuery;

public interface GetFeedUseCase {
    FeedPageResult query(GetFeedQuery query);
}
