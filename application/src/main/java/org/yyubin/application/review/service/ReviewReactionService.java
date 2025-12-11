package org.yyubin.application.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.DeleteReactionUseCase;
import org.yyubin.application.review.dto.ReviewReactionResult;
import org.yyubin.application.review.UpsertReactionUseCase;
import org.yyubin.application.review.command.DeleteReactionCommand;
import org.yyubin.application.review.command.UpsertReactionCommand;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewReactionPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewReaction;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.user.UserId;
import org.yyubin.application.notification.NotificationMessages;

@Service
@RequiredArgsConstructor
public class ReviewReactionService implements UpsertReactionUseCase, DeleteReactionUseCase {

    private final LoadReviewPort loadReviewPort;
    private final ReviewReactionPort reviewReactionPort;
    private final LoadUserPort loadUserPort;
    private final NotificationEventUseCase notificationEventUseCase;
    private final EventPublisher eventPublisher;

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
        if (!review.isWrittenBy(userId)) {
            notificationEventUseCase.handle(new NotificationEventPayload(
                    review.getUserId().value(),
                    NotificationType.LIKE_ON_REVIEW,
                    command.userId(),
                    command.reviewId(),
                    NotificationMessages.LIKE_ON_REVIEW
            ));
        }
        publishReactionEvent("REACTION_UPSERTED", saved);
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
        publishReactionEvent("REACTION_DELETED", ReviewId.of(command.reviewId()), userId);
    }

    private void publishReactionEvent(String eventType, ReviewReaction reaction) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("reviewId", reaction.getReviewId().getValue());
        metadata.put("bookId", loadReviewBookId(reaction.getReviewId().getValue()));
        eventPublisher.publish(
                EventTopics.REACTION,
                reaction.getUserId().value().toString(),
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        eventType,
                        reaction.getUserId().value(),
                        "REVIEW",
                        reaction.getReviewId().getValue().toString(),
                        metadata,
                        java.time.Instant.now(),
                        "api",
                        1
                )
        );
    }

    private void publishReactionEvent(String eventType, ReviewId reviewId, UserId userId) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("reviewId", reviewId.getValue());
        metadata.put("bookId", loadReviewBookId(reviewId.getValue()));
        eventPublisher.publish(
            EventTopics.REACTION,
            userId.value().toString(),
            new EventPayload(
                    java.util.UUID.randomUUID(),
                    eventType,
                    userId.value(),
                    "REVIEW",
                    reviewId.getValue().toString(),
                    metadata,
                    java.time.Instant.now(),
                    "api",
                    1
            )
        );
    }

    private Long loadReviewBookId(Long reviewId) {
        return loadReviewPort.loadById(reviewId).getBookId().getValue();
    }
}
