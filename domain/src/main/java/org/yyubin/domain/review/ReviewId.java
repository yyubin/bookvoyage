package org.yyubin.domain.review;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object for Review identifier
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewId {
    private final Long value;

    public static ReviewId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Review ID must be positive");
        }
        return new ReviewId(value);
    }

    @Override
    public String toString() {
        return "ReviewId{" + value + '}';
    }
}
