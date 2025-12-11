package org.yyubin.infrastructure.stream.feed;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.feed.port.FeedItemPort;
import org.yyubin.application.feed.port.FeedItemWritePort;
import org.yyubin.domain.feed.FeedItem;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
public class RedisFeedItemAdapter implements FeedItemPort, FeedItemWritePort {

    private final StringRedisTemplate stringRedisTemplate;

    private static final double MIN_SCORE = Double.NEGATIVE_INFINITY;

    @Override
    public List<FeedItem> loadFeed(UserId userId, Double cursorScore, int size) {
        String key = recommendKey(userId.value());
        double max = cursorScore != null ? cursorScore : Double.POSITIVE_INFINITY;

        Set<TypedTuple<String>> entries = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, max, MIN_SCORE, 0, size);

        List<FeedItem> result = new ArrayList<>();
        if (entries == null) {
            return result;
        }

        for (TypedTuple<String> entry : entries) {
            if (entry.getValue() == null || entry.getScore() == null) {
                continue;
            }
            Long reviewId = parseLong(entry.getValue());
            if (reviewId == null) {
                continue;
            }
            LocalDateTime createdAt = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(entry.getScore().longValue()),
                    ZoneOffset.UTC
            );
            result.add(FeedItem.of(null, userId, ReviewId.of(reviewId), createdAt));
        }
        return result;
    }

    @Override
    public FeedItem save(FeedItem feedItem) {
        String key = recommendKey(feedItem.getUserId().value());
        double score = feedItem.getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli();
        String member = "review:%d".formatted(feedItem.getReviewId().getValue());
        stringRedisTemplate.opsForZSet().add(key, member, score);
        return feedItem;
    }

    private String recommendKey(Long userId) {
        return "recommend:user:%d".formatted(userId);
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value.replaceFirst("^review:", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
