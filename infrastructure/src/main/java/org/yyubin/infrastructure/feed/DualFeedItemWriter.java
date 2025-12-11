package org.yyubin.infrastructure.feed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.yyubin.application.feed.port.FeedItemWritePort;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.infrastructure.persistence.feed.FeedItemPersistenceAdapter;
import org.yyubin.infrastructure.stream.feed.RedisFeedItemAdapter;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class DualFeedItemWriter implements FeedItemWritePort {

    private final RedisFeedItemAdapter redisWriter;
    private final FeedItemPersistenceAdapter dbWriter;

    @Override
    public FeedItem save(FeedItem feedItem) {
        FeedItem saved = dbWriter.save(feedItem);
        try {
            redisWriter.save(saved);
        } catch (Exception ex) {
            log.warn("Failed to write feed to Redis for user={} review={} : {}", feedItem.getUserId().value(), feedItem.getReviewId().getValue(), ex.toString());
        }
        return saved;
    }
}
