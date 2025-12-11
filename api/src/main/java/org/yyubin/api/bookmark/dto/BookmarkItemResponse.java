package org.yyubin.api.bookmark.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.bookmark.dto.ReviewBookmarkItem;

public record BookmarkItemResponse(
        Long bookmarkId,
        Long reviewId,
        LocalDateTime createdAt,
        Long bookId,
        String title,
        List<String> authors,
        String coverUrl,
        String publisher,
        String publishedDate,
        int rating,
        String content,
        Long reviewerId
) {
    public static BookmarkItemResponse from(ReviewBookmarkItem item) {
        return new BookmarkItemResponse(
                item.bookmarkId(),
                item.reviewId(),
                item.createdAt(),
                item.bookId(),
                item.title(),
                item.authors(),
                item.coverUrl(),
                item.publisher(),
                item.publishedDate(),
                item.rating(),
                item.content(),
                item.reviewerId()
        );
    }
}
