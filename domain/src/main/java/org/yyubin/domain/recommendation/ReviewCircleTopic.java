package org.yyubin.domain.recommendation;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 리뷰 서클 토픽
 * 비슷한 취향의 사용자들이 지금 이야기하는 주제
 * Value Object - 불변 객체
 */
public record ReviewCircleTopic(
    String keyword,
    int reviewCount,
    double score,
    LocalDateTime lastActivityAt
) implements Comparable<ReviewCircleTopic> {

    public ReviewCircleTopic {
        Objects.requireNonNull(keyword, "keyword cannot be null");
        if (keyword.isBlank()) {
            throw new IllegalArgumentException("keyword cannot be blank");
        }
        if (reviewCount < 0) {
            throw new IllegalArgumentException("reviewCount cannot be negative");
        }
        if (score < 0.0) {
            throw new IllegalArgumentException("score cannot be negative");
        }
        Objects.requireNonNull(lastActivityAt, "lastActivityAt cannot be null");
    }

    public static ReviewCircleTopic of(String keyword, int reviewCount, double score) {
        return new ReviewCircleTopic(keyword, reviewCount, score, LocalDateTime.now());
    }

    public static ReviewCircleTopic of(String keyword, int reviewCount, double score, LocalDateTime lastActivityAt) {
        return new ReviewCircleTopic(keyword, reviewCount, score, lastActivityAt);
    }

    @Override
    public int compareTo(ReviewCircleTopic other) {
        return Double.compare(other.score, this.score);
    }
}
