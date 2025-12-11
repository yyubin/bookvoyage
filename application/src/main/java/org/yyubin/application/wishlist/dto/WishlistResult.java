package org.yyubin.application.wishlist.dto;

import java.util.Collections;
import java.util.List;

public record WishlistResult(
        List<WishlistItemResult> items
) {
    public WishlistResult {
        items = items == null ? Collections.emptyList() : List.copyOf(items);
    }
}
