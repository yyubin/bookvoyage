package org.yyubin.application.wishlist;

import org.yyubin.application.wishlist.dto.WishlistResult;
import org.yyubin.application.wishlist.query.GetWishlistQuery;

public interface GetWishlistUseCase {
    WishlistResult query(GetWishlistQuery query);
}
