package org.yyubin.domain.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ReviewValueObjectTest {

    @Test
    void bookGenreFromValid() {
        assertEquals(BookGenre.FICTION, BookGenre.from("FICTION"));
    }

    @Test
    void bookGenreFromInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> BookGenre.from("invalid"));
    }

    @Test
    void reviewVisibilityFromValid() {
        assertEquals(ReviewVisibility.PUBLIC, ReviewVisibility.from("PUBLIC"));
    }

    @Test
    void reviewVisibilityFromInvalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> ReviewVisibility.from("nope"));
    }

    @Test
    void ratingBounds() {
        assertThrows(IllegalArgumentException.class, () -> Rating.of(0));
        assertThrows(IllegalArgumentException.class, () -> Rating.of(6));
        assertEquals(5, Rating.of(5).getValue());
    }
}
