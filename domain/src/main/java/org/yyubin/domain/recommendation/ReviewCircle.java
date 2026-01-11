package org.yyubin.domain.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 리뷰 서클 - 비슷한 취향의 독자들이 지금 이야기하는 주제 집합
 * Value Object - 불변 객체
 */
public record ReviewCircle(
    Long userId,
    String window,  // "24h", "7d"
    List<ReviewCircleTopic> topics,
    int similarUserCount,
    LocalDateTime calculatedAt
) {

    public ReviewCircle {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(window, "window cannot be null");
        Objects.requireNonNull(topics, "topics cannot be null");
        if (similarUserCount < 0) {
            throw new IllegalArgumentException("similarUserCount cannot be negative");
        }
        Objects.requireNonNull(calculatedAt, "calculatedAt cannot be null");
    }

    public static ReviewCircle of(
        Long userId,
        String window,
        List<ReviewCircleTopic> topics,
        int similarUserCount
    ) {
        return new ReviewCircle(userId, window, topics, similarUserCount, LocalDateTime.now());
    }

    public List<ReviewCircleTopic> getTopTopics(int limit) {
        return topics.stream()
            .sorted()
            .limit(limit)
            .toList();
    }
}
