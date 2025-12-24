package org.yyubin.domain.review;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Highlight {

    private static final int MAX_LENGTH = 100;

    private final HighlightId id;
    private final String rawValue;
    private final String normalizedValue;
    private final LocalDateTime createdAt;

    private Highlight(HighlightId id, String rawValue, String normalizedValue, LocalDateTime createdAt) {
        this.id = id;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.createdAt = createdAt;
    }

    public static Highlight create(String rawValue, HighlightNormalizer normalizer) {
        Objects.requireNonNull(normalizer, "Highlight normalizer cannot be null");
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Highlight value cannot be empty");
        }
        if (rawValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Highlight value cannot exceed " + MAX_LENGTH + " characters");
        }
        return new Highlight(
                null,
                rawValue,
                normalizer.normalize(rawValue),
                LocalDateTime.now()
        );
    }

    public Highlight withId(Long id) {
        return new Highlight(new HighlightId(id), rawValue, normalizedValue, createdAt);
    }

    public static Highlight of(HighlightId id, String rawValue, String normalizedValue, LocalDateTime createdAt) {
        Objects.requireNonNull(id, "Highlight ID cannot be null");
        Objects.requireNonNull(rawValue, "Raw value cannot be null");
        Objects.requireNonNull(normalizedValue, "Normalized value cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");
        return new Highlight(id, rawValue, normalizedValue, createdAt);
    }
}
