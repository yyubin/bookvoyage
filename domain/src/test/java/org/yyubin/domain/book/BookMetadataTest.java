package org.yyubin.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class BookMetadataTest {

    @Test
    void createValidMetadata() {
        BookMetadata metadata = BookMetadata.of(
                "Title",
                List.of("Author1", "Author2"),
                "isbn10",
                "isbn13",
                "cover",
                "publisher",
                "2024-01-01",
                "desc",
                "ko",
                300,
                "vol"
        );

        assertEquals("Title", metadata.getTitle());
        assertEquals(List.of("Author1", "Author2"), metadata.getAuthors());
        assertEquals("isbn13", metadata.getIsbn13());
    }

    @Test
    void emptyTitleThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                BookMetadata.of("", List.of("Author"), null, null, null, null, null, null, null, null, null)
        );
    }

    @Test
    void emptyAuthorsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                BookMetadata.of("Title", List.of(), null, null, null, null, null, null, null, null, null)
        );
    }
}
