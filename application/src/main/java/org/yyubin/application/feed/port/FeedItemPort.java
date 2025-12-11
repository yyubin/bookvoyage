package org.yyubin.application.feed.port;

import java.util.List;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.user.UserId;

public interface FeedItemPort {
    List<FeedItem> loadFeed(UserId userId, Double cursorScore, int size);
}
