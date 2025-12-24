package org.yyubin.domain.userbook;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadingCount {
    private final int count;

    public static ReadingCount of(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Reading count must be at least 1");
        }
        return new ReadingCount(count);
    }

    public static ReadingCount first() {
        return new ReadingCount(1);
    }

    public ReadingCount increment() {
        return new ReadingCount(count + 1);
    }

    public int getCount() {
        return count;
    }
}
