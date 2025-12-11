package org.yyubin.api.wishlist.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.wishlist.dto.WishlistItemResult;

public record WishlistItemResponse(
        Long wishlistId,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        String publisher,
        String publishedDate,
        LocalDateTime createdAt
) {

    public static WishlistItemResponse from(WishlistItemResult result) {
        return new WishlistItemResponse(
                result.wishlistId(),
                result.bookId(),
                result.title(),
                result.authors(),
                result.coverUrl(),
                result.publisher(),
                result.publishedDate(),
                result.createdAt()
        );
    }
}
