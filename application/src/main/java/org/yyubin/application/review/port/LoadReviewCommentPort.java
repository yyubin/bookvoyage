package org.yyubin.application.review.port;

import org.yyubin.domain.review.ReviewComment;

public interface LoadReviewCommentPort {

    ReviewComment loadById(Long commentId);

    java.util.List<ReviewComment> loadByReviewId(Long reviewId, Long cursor, int size);

    long countByReviewId(Long reviewId);

    java.util.List<ReviewComment> loadRepliesByParentId(Long parentCommentId, Long cursor, int size);

    long countRepliesByParentId(Long parentCommentId);

    java.util.Map<Long, Long> countRepliesBatch(java.util.List<Long> parentCommentIds);
}
