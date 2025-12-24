package org.yyubin.application.review.port;

import java.util.List;
import org.yyubin.domain.review.Review;

public interface LoadReviewPort {
    Review loadById(Long reviewId);

    List<Review> loadByUserId(Long userId, Long viewerId, Long cursor, int size);

    List<Review> loadByHighlightNormalized(String normalizedHighlight, Long cursor, int size);
}
