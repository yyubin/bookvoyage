package org.yyubin.application.wishlist.command;

import org.yyubin.domain.book.BookSearchItem;

public record AddWishlistCommand(
        Long userId,
        BookSearchItem bookSearchItem
) {
}
