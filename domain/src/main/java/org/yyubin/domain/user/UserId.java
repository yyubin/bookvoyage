package org.yyubin.domain.user;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object for User identifier
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserId {
    private final Long value;

    public static UserId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        return new UserId(value);
    }

    @Override
    public String toString() {
        return "UserId{" + value + '}';
    }
}
