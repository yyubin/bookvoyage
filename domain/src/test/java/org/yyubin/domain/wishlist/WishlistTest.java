package org.yyubin.domain.wishlist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.user.UserId;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Wishlist domain tests")
class WishlistTest {

    @Test
    @DisplayName("of validates required fields")
    void ofValidatesRequiredFields() {
        assertThatThrownBy(() -> Wishlist.of(1L, null, BookId.of(2L), LocalDateTime.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("create builds wishlist with null id")
    void createBuildsWishlist() {
        Wishlist wishlist = Wishlist.create(new UserId(1L), BookId.of(2L));

        assertThat(wishlist.getId()).isNull();
        assertThat(wishlist.getCreatedAt()).isNotNull();
    }
}
