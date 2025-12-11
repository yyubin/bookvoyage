package org.yyubin.application.wishlist.query;

public record GetWishlistQuery(
        Long userId,
        WishlistSort sort
) {
}
