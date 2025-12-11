package org.yyubin.application.wishlist.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.wishlist.Wishlist;

public record WishlistItemResult(
        Long wishlistId,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        String publisher,
        String publishedDate,
        LocalDateTime createdAt
) {
    public static WishlistItemResult from(Wishlist wishlist, Book book) {
        return new WishlistItemResult(
                wishlist.getId(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthors(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getPublisher(),
                book.getMetadata().getPublishedDate(),
                wishlist.getCreatedAt()
        );
    }
}
