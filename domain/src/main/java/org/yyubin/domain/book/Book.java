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

    public static Book create(
            String title,
            java.util.List<String> authors,
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
        return new Book(
                null,
                BookMetadata.of(
                        title,
                        authors,
                        isbn10,
                        isbn13,
                        coverUrl,
                        publisher,
                        publishedDate,
                        description,
                        language,
                        pageCount,
                        googleVolumeId
                )
        );
    }

    public Book updateMetadata(BookMetadata newMetadata) {
        return new Book(this.id, newMetadata);
    }
}
