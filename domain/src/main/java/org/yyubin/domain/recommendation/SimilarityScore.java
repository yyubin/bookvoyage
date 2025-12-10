package org.yyubin.domain.recommendation;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object representing similarity score between 0.0 and 1.0
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SimilarityScore {
    private static final double MIN_SCORE = 0.0;
    private static final double MAX_SCORE = 1.0;

    private final double value;

    public static SimilarityScore of(double value) {
        if (value < MIN_SCORE || value > MAX_SCORE) {
            throw new IllegalArgumentException(
                    String.format("Similarity score must be between %.1f and %.1f", MIN_SCORE, MAX_SCORE)
            );
        }
        return new SimilarityScore(value);
    }

    public static SimilarityScore zero() {
        return new SimilarityScore(MIN_SCORE);
    }

    public static SimilarityScore max() {
        return new SimilarityScore(MAX_SCORE);
    }

    public boolean isHighSimilarity() {
        return value >= 0.7;
    }

    public boolean isLowSimilarity() {
        return value < 0.3;
    }

    @Override
    public String toString() {
        return "SimilarityScore{" + value + '}';
    }
}
