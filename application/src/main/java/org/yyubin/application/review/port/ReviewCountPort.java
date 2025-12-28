package org.yyubin.application.review.port;

public interface ReviewCountPort {
    long countByBookId(Long bookId);
    Double calculateAverageRating(Long bookId);
    long countByUserId(Long userId);
}
