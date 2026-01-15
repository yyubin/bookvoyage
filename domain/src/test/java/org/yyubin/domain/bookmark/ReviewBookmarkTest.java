package org.yyubin.domain.bookmark;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ReviewBookmark record tests")
class ReviewBookmarkTest {

    @Test
    @DisplayName("constructor enforces non-null fields")
    void constructorRequiresFields() {
        assertThatThrownBy(() -> new ReviewBookmark(1L, null, ReviewId.of(2L), LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("create assigns current time and null id")
    void createAssignsValues() {
        ReviewBookmark bookmark = ReviewBookmark.create(new UserId(1L), ReviewId.of(2L));

        assertThat(bookmark.id()).isNull();
        assertThat(bookmark.createdAt()).isNotNull();
    }
}
