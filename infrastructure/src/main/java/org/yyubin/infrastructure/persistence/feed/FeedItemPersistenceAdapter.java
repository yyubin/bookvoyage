package org.yyubin.infrastructure.persistence.feed;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.feed.port.FeedItemPort;
import org.yyubin.application.feed.port.FeedItemWritePort;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedItemPersistenceAdapter implements FeedItemPort, FeedItemWritePort {

    private final FeedItemJpaRepository feedItemJpaRepository;

    @Override
    public List<FeedItem> loadFeed(UserId userId, Double cursorScore, int size) {
        LocalDateTime cursor = cursorScore != null
                ? LocalDateTime.ofEpochSecond((long) (cursorScore / 1000), 0, java.time.ZoneOffset.UTC)
                : null;
        List<FeedItemEntity> entities;
        if (cursor != null) {
            entities = feedItemJpaRepository.findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                    userId.value(), cursor, PageRequest.of(0, size)
            );
        } else {
            entities = feedItemJpaRepository.findByUserIdOrderByCreatedAtDesc(
                    userId.value(), PageRequest.of(0, size)
            );
        }
        return entities.stream().map(FeedItemEntity::toDomain).toList();
    }

    @Override
    @Transactional
    public FeedItem save(FeedItem feedItem) {
        return feedItemJpaRepository.save(FeedItemEntity.fromDomain(feedItem)).toDomain();
    }
}
