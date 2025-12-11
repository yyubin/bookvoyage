package org.yyubin.domain.book;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Value Object for Book metadata
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BookMetadata {
    private final String title;
    private final List<String> authors;
    private final String isbn10;
    private final String isbn13;
    private final String coverUrl;
    private final String publisher;
    private final String publishedDate;
    private final String language;
    private final Integer pageCount;
    private final String description;
    private final String googleVolumeId;

    public static BookMetadata of(
            String title,
            List<String> authors,
            String isbn10,
            String isbn13,
            String coverUrl,
            String publisher,
            String publishedDate,
            String description,
            String language,
            Integer pageCount,
            String googleVolumeId
    ) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        List<String> normalizedAuthors = normalizeAuthors(authors);
        return new BookMetadata(
                title,
                normalizedAuthors,
                isbn10,
                isbn13,
                coverUrl,
                publisher,
                publishedDate,
                language,
                pageCount,
                description != null ? description : "",
                googleVolumeId
        );
    }

    private static List<String> normalizeAuthors(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            throw new IllegalArgumentException("Book authors cannot be empty");
        }
        List<String> cleaned = authors.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Book authors cannot be empty");
        }
        return Collections.unmodifiableList(cleaned);
    }
}
