package org.yyubin.batch.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.comment.ReviewCommentJpaRepository;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionJpaRepository;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSyncDataProvider {

    private final ReviewReactionJpaRepository reviewReactionJpaRepository;
    private final ReviewBookmarkJpaRepository reviewBookmarkJpaRepository;
    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    public ReviewSyncDto build(ReviewEntity review) {
        int likeCount = safeCount(reviewReactionJpaRepository.countByReviewId(review.getId()));
        int bookmarkCount = safeCount(reviewBookmarkJpaRepository.countByReviewId(review.getId()));
        int commentCount = safeCount(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(review.getId()));
        long viewCount = review.getViewCount() != null ? review.getViewCount() : 0L;

        return new ReviewSyncDto(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                review.getContent(),
                review.getRating() != null ? review.getRating().floatValue() : null,
                review.getVisibility() != null ? review.getVisibility().name() : null,
                review.getCreatedAt(),
                likeCount,
                bookmarkCount,
                commentCount,
                viewCount,
                null // dwellScore placeholder
        );
    }

    private int safeCount(long value) {
        if (value > Integer.MAX_VALUE) {
            log.warn("Review metric overflow detected, capping at Integer.MAX_VALUE");
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
