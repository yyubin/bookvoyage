package org.yyubin.domain.review;

public record KeywordId(Long value) {
    public KeywordId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Invalid keyword ID");
        }
    }
}
