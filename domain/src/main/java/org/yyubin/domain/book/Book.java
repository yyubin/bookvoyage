package org.yyubin.domain.book;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;

/**
 * Book Aggregate Root
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Book {
    private final BookId id;
    private final BookMetadata metadata;

    public static Book of(BookId id, BookMetadata metadata) {
        Objects.requireNonNull(metadata, "Book metadata cannot be null");
        return new Book(id, metadata);
    }

    public static Book create(String title, String author, String isbn, String coverUrl, String description) {
        return new Book(
                null,
                BookMetadata.of(title, author, isbn, coverUrl, description)
        );
    }

    public Book updateMetadata(BookMetadata newMetadata) {
        return new Book(this.id, newMetadata);
    }
}
