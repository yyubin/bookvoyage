package org.yyubin.application.review.port;

import org.yyubin.domain.review.Review;

public interface SaveReviewPort {
    Review save(Review review);
}
