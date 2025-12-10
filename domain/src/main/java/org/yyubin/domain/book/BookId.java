package org.yyubin.domain.book;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Value Object for Book identifier
 */
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BookId {
    private final Long value;

    public static BookId of(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Book ID must be positive");
        }
        return new BookId(value);
    }

    @Override
    public String toString() {
        return "BookId{" + value + '}';
    }
}
