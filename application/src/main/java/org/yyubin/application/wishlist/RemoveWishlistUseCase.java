package org.yyubin.application.wishlist;

import org.yyubin.application.wishlist.command.RemoveWishlistCommand;

public interface RemoveWishlistUseCase {
    void remove(RemoveWishlistCommand command);
}
