package org.yyubin.api.wishlist.dto;

import java.util.List;
import org.yyubin.application.wishlist.dto.WishlistResult;

public record WishlistResponse(
        List<WishlistItemResponse> items
) {
    public static WishlistResponse from(WishlistResult result) {
        return new WishlistResponse(
                result.items().stream()
                        .map(WishlistItemResponse::from)
                        .toList()
        );
    }
}
