package org.yyubin.application.book.port;

import java.util.List;

public interface SearchBookReviewsPort {
    
    SearchResult searchByBookId(Long bookId, Long cursor, int size, String sort);
    
    record SearchResult(
            List<ReviewDocument> reviews,
            Long nextCursor,
            long totalCount
    ) {
    }
    
    record ReviewDocument(
            Long reviewId,
            Long userId,
            String title,
            Float rating,
            String content,
            java.time.LocalDateTime createdAt,
            Integer likeCount,
            Integer commentCount,
            Long viewCount
    ) {
    }
}
