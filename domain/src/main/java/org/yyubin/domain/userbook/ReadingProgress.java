package org.yyubin.domain.userbook;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ReadingProgress {
    private final int percentage;

    public static ReadingProgress of(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Progress must be between 0 and 100");
        }
        return new ReadingProgress(percentage);
    }

    public static ReadingProgress notStarted() {
        return new ReadingProgress(0);
    }

    public static ReadingProgress completed() {
        return new ReadingProgress(100);
    }

    public boolean isComplete() {
        return percentage == 100;
    }

    public boolean isNotStarted() {
        return percentage == 0;
    }
}
