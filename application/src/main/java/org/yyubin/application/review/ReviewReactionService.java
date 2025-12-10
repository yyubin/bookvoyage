package org.yyubin.application.review;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.command.DeleteReactionCommand;
import org.yyubin.application.review.command.UpsertReactionCommand;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewReactionPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewReaction;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
public class ReviewReactionService implements UpsertReactionUseCase, DeleteReactionUseCase {

    private final LoadReviewPort loadReviewPort;
    private final ReviewReactionPort reviewReactionPort;
    private final LoadUserPort loadUserPort;

    @Override
    @Transactional
    public ReviewReactionResult execute(UpsertReactionCommand command) {
        UserId userId = new UserId(command.userId());
        loadUserPort.loadById(userId);

        Review review = loadReviewPort.loadById(command.reviewId());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }
        if (!review.getVisibility().isPublic() && !review.isWrittenBy(userId)) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }

        ReviewReaction reaction = reviewReactionPort.loadByReviewIdAndUserId(command.reviewId(), command.userId())
                .map(existing -> ReviewReaction.of(
                        existing.getId(),
                        ReviewId.of(command.reviewId()),
                        userId,
                        command.content(),
                        existing.getCreatedAt()
                ))
                .orElseGet(() -> ReviewReaction.create(
                        ReviewId.of(command.reviewId()),
                        userId,
                        command.content()
                ));

        ReviewReaction saved = reviewReactionPort.save(reaction);
        return ReviewReactionResult.from(saved);
    }

    @Override
    @Transactional
    public void execute(DeleteReactionCommand command) {
        UserId userId = new UserId(command.userId());
        loadUserPort.loadById(userId);

        Review review = loadReviewPort.loadById(command.reviewId());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }
        if (!review.getVisibility().isPublic() && !review.isWrittenBy(userId)) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }

        reviewReactionPort.delete(command.reviewId(), command.userId());
    }
}
