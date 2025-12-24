package org.yyubin.domain.userbook;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PersonalRating {
    private final Integer value;

    public static PersonalRating of(Integer value) {
        if (value != null && (value < 1 || value > 5)) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return new PersonalRating(value);
    }

    public static PersonalRating empty() {
        return new PersonalRating(null);
    }

    public boolean hasRating() {
        return value != null;
    }

    public Integer getValue() {
        return value;
    }
}
