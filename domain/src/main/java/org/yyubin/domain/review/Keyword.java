package org.yyubin.domain.review;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;

@Getter
public class Keyword {

    private final KeywordId id;
    private final String rawValue;
    private final String normalizedValue;
    private final LocalDateTime createdAt;

    private Keyword(KeywordId id,
                    String rawValue,
                    String normalizedValue,
                    LocalDateTime createdAt) {
        this.id = id;
        this.rawValue = rawValue;
        this.normalizedValue = normalizedValue;
        this.createdAt = createdAt;
    }

    public static Keyword create(String rawValue, KeywordNormalizer normalizer) {
        Objects.requireNonNull(normalizer, "Keyword normalizer cannot be null");
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Keyword value cannot be empty");
        }
        return new Keyword(
                null,
                rawValue,
                normalizer.normalize(rawValue),
                LocalDateTime.now()
        );
    }

    public Keyword withId(Long id) {
        return new Keyword(new KeywordId(id), rawValue, normalizedValue, createdAt);
    }

    public static Keyword of(KeywordId id, String rawValue, String normalizedValue, LocalDateTime createdAt) {
        Objects.requireNonNull(id, "Keyword ID cannot be null");
        Objects.requireNonNull(rawValue, "Raw value cannot be null");
        Objects.requireNonNull(normalizedValue, "Normalized value cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");
        return new Keyword(id, rawValue, normalizedValue, createdAt);
    }
}
