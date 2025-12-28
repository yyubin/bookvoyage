package org.yyubin.application.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.ToggleReviewLikeUseCase;
import org.yyubin.application.review.command.ToggleReviewLikeCommand;
import org.yyubin.application.review.port.ReviewLikePort;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewLike;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewLikeService implements ToggleReviewLikeUseCase {

    private final ReviewLikePort reviewLikePort;

    @Override
    public ToggleResult execute(ToggleReviewLikeCommand command) {
        ReviewId reviewId = ReviewId.of(command.reviewId());
        UserId userId = new UserId(command.userId());

        boolean exists = reviewLikePort.exists(reviewId, userId);

        if (exists) {
            // Unlike
            reviewLikePort.delete(reviewId, userId);
            long likeCount = reviewLikePort.countByReviewId(reviewId);
            return new ToggleResult(false, likeCount);
        } else {
            // Like
            ReviewLike reviewLike = ReviewLike.create(reviewId, userId);
            reviewLikePort.save(reviewLike);
            long likeCount = reviewLikePort.countByReviewId(reviewId);
            return new ToggleResult(true, likeCount);
        }
    }
}
