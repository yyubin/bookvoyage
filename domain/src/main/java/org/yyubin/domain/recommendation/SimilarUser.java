package org.yyubin.domain.recommendation;

import java.util.Objects;

/**
 * 유사 사용자
 * Value Object - 불변 객체
 */
public record SimilarUser(
    Long userId,
    double similarityScore  // 0.0 ~ 1.0
) implements Comparable<SimilarUser> {

    public SimilarUser {
        Objects.requireNonNull(userId, "userId cannot be null");
        if (similarityScore < 0.0 || similarityScore > 1.0) {
            throw new IllegalArgumentException("Similarity score must be between 0.0 and 1.0");
        }
    }

    public static SimilarUser of(Long userId, double similarityScore) {
        return new SimilarUser(userId, similarityScore);
    }

    @Override
    public int compareTo(SimilarUser other) {
        return Double.compare(other.similarityScore, this.similarityScore);
    }
}
