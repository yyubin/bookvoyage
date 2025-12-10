package org.yyubin.domain.review;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewReactionId {
    private final Long value;

    public static ReviewReactionId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Reaction ID must be positive");
        }
        return new ReviewReactionId(value);
    }

    @Override
    public String toString() {
        return "ReviewReactionId{" + value + '}';
    }
}
