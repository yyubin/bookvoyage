package org.yyubin.domain.userbook;

public enum ReadingStatus {
    WANT_TO_READ,
    READING,
    COMPLETED;

    public static ReadingStatus from(String value) {
        if (value == null || value.isBlank()) {
            return WANT_TO_READ;
        }
        try {
            return ReadingStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid reading status: " + value);
        }
    }

    public boolean isWantToRead() {
        return this == WANT_TO_READ;
    }

    public boolean isReading() {
        return this == READING;
    }

    public boolean isCompleted() {
        return this == COMPLETED;
    }
}
