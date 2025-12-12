package org.yyubin.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class BookSearchItemTest {

    @Test
    void holdsGoogleVolumeIdAndAuthors() {
        BookSearchItem item = BookSearchItem.of(
                "Title",
                List.of("Author"),
                "isbn10",
                "isbn13",
                "cover",
                "publisher",
                "2024-01-01",
                "desc",
                "ko",
                123,
                "vol123"
        );

        assertEquals("vol123", item.getGoogleVolumeId());
        assertEquals(List.of("Author"), item.getAuthors());
    }
}
