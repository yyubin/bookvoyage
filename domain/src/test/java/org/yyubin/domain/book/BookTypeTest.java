package org.yyubin.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookType enum tests")
class BookTypeTest {

    @Test
    @DisplayName("getDefault returns published book")
    void getDefaultReturnsPublished() {
        assertThat(BookType.getDefault()).isEqualTo(BookType.PUBLISHED_BOOK);
    }
}
