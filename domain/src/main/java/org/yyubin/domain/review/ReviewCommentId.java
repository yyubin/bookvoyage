package org.yyubin.domain.review;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewCommentId {
    private final Long value;

    public static ReviewCommentId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Comment ID must be positive");
        }
        return new ReviewCommentId(value);
    }

    @Override
    public String toString() {
        return "ReviewCommentId{" + value + '}';
    }
}
