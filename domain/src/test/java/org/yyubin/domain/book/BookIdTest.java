package org.yyubin.domain.book;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BookIdTest {

    @Test
    void validBookId() {
        BookId id = BookId.of(1L);
        assertEquals(1L, id.getValue());
    }

    @Test
    void invalidBookIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> BookId.of(0L));
        assertThrows(IllegalArgumentException.class, () -> BookId.of(null));
    }
}
