package org.yyubin.batch.sync;

import java.time.LocalDateTime;
import java.util.List;

public record UserSyncDto(
        Long id,
        String username,
        String email,
        LocalDateTime createdAt,
        List<ViewedBook> viewedBooks,
        List<WishlistedBook> wishlistedBooks,
        List<LikedReviewBook> likedReviewBooks
        ) {

    public record ViewedBook(
            Long bookId,
            LocalDateTime firstViewedAt,
            LocalDateTime lastViewedAt,
            int viewCount
    ) { }

    public record WishlistedBook(
            Long bookId,
            LocalDateTime addedAt
    ) { }

    public record LikedReviewBook(
            Long reviewId,
            Long bookId,
            LocalDateTime likedAt
    ) { }
}
