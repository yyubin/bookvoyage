package org.yyubin.infrastructure.feed;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.yyubin.application.feed.port.FeedItemPort;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.stream.feed.RedisFeedItemAdapter;
import org.yyubin.infrastructure.persistence.feed.FeedItemPersistenceAdapter;

@Component
@Primary
@RequiredArgsConstructor
public class FallbackFeedService implements FeedItemPort {

    private final RedisFeedItemAdapter redisAdapter;
    private final FeedItemPersistenceAdapter dbAdapter;

    @Override
    public List<FeedItem> loadFeed(UserId userId, Double cursorScore, int size) {
        List<FeedItem> fromRedis = redisAdapter.loadFeed(userId, cursorScore, size);
        if (fromRedis != null && !fromRedis.isEmpty()) {
            return trim(fromRedis, size);
        }
        return dbAdapter.loadFeed(userId, cursorScore, size);
    }

    private List<FeedItem> trim(List<FeedItem> items, int size) {
        if (items.size() <= size) {
            return items;
        }
        return new ArrayList<>(items.subList(0, size));
    }
}
