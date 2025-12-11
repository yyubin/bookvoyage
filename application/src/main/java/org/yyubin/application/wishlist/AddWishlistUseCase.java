package org.yyubin.application.wishlist;

import org.yyubin.application.wishlist.command.AddWishlistCommand;

public interface AddWishlistUseCase {
    void add(AddWishlistCommand command);
}
