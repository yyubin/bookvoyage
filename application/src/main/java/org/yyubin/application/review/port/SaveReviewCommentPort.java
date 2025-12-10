package org.yyubin.application.review.port;

import org.yyubin.domain.review.ReviewComment;

public interface SaveReviewCommentPort {

    ReviewComment save(ReviewComment comment);
}
