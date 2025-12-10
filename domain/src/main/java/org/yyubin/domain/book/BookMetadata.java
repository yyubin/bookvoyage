package org.yyubin.domain.book;

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
    private final String author;
    private final String isbn;
    private final String coverUrl;
    private final String description;

    public static BookMetadata of(String title, String author, String isbn, String coverUrl, String description) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Book author cannot be empty");
        }

        return new BookMetadata(title, author, isbn, coverUrl, description != null ? description : "");
    }
}
