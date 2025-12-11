package org.yyubin.domain.book;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class BookSearchItem {

    private final String title;
    private final List<String> authors;
    private final String isbn10;
    private final String isbn13;
    private final String coverUrl;
    private final String publisher;
    private final String publishedDate;
    private final String description;
    private final String language;
    private final Integer pageCount;

    private BookSearchItem(
            String title,
            List<String> authors,
            String isbn10,
            String isbn13,
            String coverUrl,
            String publisher,
            String publishedDate,
            String description,
            String language,
            Integer pageCount
    ) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.authors = authors == null ? Collections.emptyList() : List.copyOf(authors);
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
        this.coverUrl = coverUrl;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.language = language;
        this.pageCount = pageCount;
    }

    public static BookSearchItem of(
            String title,
            List<String> authors,
            String isbn10,
            String isbn13,
            String coverUrl,
            String publisher,
            String publishedDate,
            String description,
            String language,
            Integer pageCount
    ) {
        return new BookSearchItem(
                title,
                authors,
                isbn10,
                isbn13,
                coverUrl,
                publisher,
                publishedDate,
                description,
                language,
                pageCount
        );
    }
}
