package org.yyubin.domain.review;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object for Review rating (1-5 stars)
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Rating {
    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    private final int value;

    public static Rating of(int value) {
        if (value < MIN_RATING || value > MAX_RATING) {
            throw new IllegalArgumentException(
                    String.format("Rating must be between %d and %d", MIN_RATING, MAX_RATING)
            );
        }
        return new Rating(value);
    }

    public boolean isPositive() {
        return value >= 4;
    }

    public boolean isNegative() {
        return value <= 2;
    }

    @Override
    public String toString() {
        return "Rating{" + value + '}';
    }
}
