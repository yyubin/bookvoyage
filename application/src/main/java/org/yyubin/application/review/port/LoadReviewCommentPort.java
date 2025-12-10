package org.yyubin.application.review.port;

import org.yyubin.domain.review.ReviewComment;

public interface LoadReviewCommentPort {

    ReviewComment loadById(Long commentId);

    java.util.List<ReviewComment> loadByReviewId(Long reviewId, Long cursor, int size);
}
