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
import org.yyubin.recommendation.service.ReviewRecommendationResult;
import org.yyubin.recommendation.service.ReviewRecommendationService;
import org.yyubin.infrastructure.persistence.feed.FeedItemPersistenceAdapter;

@Component
@Primary
@RequiredArgsConstructor
public class FallbackFeedService implements FeedItemPort {

    private final RedisFeedItemAdapter redisAdapter;
    private final FeedItemPersistenceAdapter dbAdapter;
    private final ReviewRecommendationService reviewRecommendationService;

    @Override
    public List<FeedItem> loadFeed(UserId userId, Double cursorScore, int size) {
        List<FeedItem> fromRedis = redisAdapter.loadFeed(userId, cursorScore, size);
        if (fromRedis != null && !fromRedis.isEmpty()) {
            return trim(fromRedis, size);
        }

        // Redis가 비어있으면 리뷰 추천을 생성하여 워밍
        List<FeedItem> warm = warmUpFromRecommendations(userId, size);
        if (!warm.isEmpty()) {
            return trim(warm, size);
        }

        return dbAdapter.loadFeed(userId, cursorScore, size);
    }

    private List<FeedItem> trim(List<FeedItem> items, int size) {
        if (items.size() <= size) {
            return items;
        }
        return new ArrayList<>(items.subList(0, size));
    }

    private List<FeedItem> warmUpFromRecommendations(UserId userId, int size) {
        List<ReviewRecommendationResult> recs =
                reviewRecommendationService.recommendFeed(userId.value(), size, false);
        if (recs.isEmpty()) {
            return List.of();
        }

        List<FeedItem> items = new ArrayList<>();
        for (ReviewRecommendationResult rec : recs) {
            if (rec.getReviewId() == null) {
                continue;
            }
            java.time.LocalDateTime createdAt =
                    rec.getCreatedAt() != null ? rec.getCreatedAt() : java.time.LocalDateTime.now();
            FeedItem item = FeedItem.of(null, userId, org.yyubin.domain.review.ReviewId.of(rec.getReviewId()), createdAt);
            items.add(item);
            redisAdapter.save(item);
        }
        return items;
    }
}
