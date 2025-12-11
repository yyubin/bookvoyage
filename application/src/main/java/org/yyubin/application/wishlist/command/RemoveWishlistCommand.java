package org.yyubin.application.wishlist.command;

public record RemoveWishlistCommand(
        Long userId,
        Long bookId
) {
}
