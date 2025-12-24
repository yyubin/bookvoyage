package org.yyubin.api.profile.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.bookmark.dto.ReviewBookmarkItem;

public record BookmarkedReviewItemResponse(
        Long reviewId,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        int rating,
        String content,
        LocalDateTime bookmarkedAt,
        String reviewerNickname
) {
    public static BookmarkedReviewItemResponse from(ReviewBookmarkItem item) {
        return new BookmarkedReviewItemResponse(
                item.reviewId(),
                item.bookId(),
                item.title(),
                item.authors(),
                item.coverUrl(),
                item.rating(),
                item.content(),
                item.createdAt(),
                item.reviewerNickname()
        );
    }
}
