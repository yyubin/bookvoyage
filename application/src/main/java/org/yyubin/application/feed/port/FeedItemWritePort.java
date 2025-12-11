package org.yyubin.application.feed.port;

import org.yyubin.domain.feed.FeedItem;

public interface FeedItemWritePort {
    FeedItem save(FeedItem feedItem);
}
