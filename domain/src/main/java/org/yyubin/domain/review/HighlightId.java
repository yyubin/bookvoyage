package org.yyubin.domain.review;

public class HighlightId {

    private final Long value;

    public HighlightId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Highlight ID must be positive");
        }
        this.value = value;
    }

    public Long value() {
        return value;
    }

    @Override
    public String toString() {
        return "HighlightId{" + value + '}';
    }
}
