package org.yyubin.infrastructure.stream.kafka;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.notification.NotificationMessages;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.user.UserId;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final LoadReviewPort loadReviewPort;
    private final LoadUserPort loadUserPort;
    private final NotificationEventUseCase notificationEventUseCase;

    @KafkaListener(
            topics = {"events.reaction", "events.review", "events.user"},
            groupId = "cg-notification"
    )
    public void consume(EventPayload payload) {
        if (payload == null || payload.eventType() == null) {
            return;
        }
        switch (payload.eventType()) {
            case "REACTION_UPSERTED" -> handleReaction(payload);
            case "COMMENT_CREATED" -> handleComment(payload);
            case "MENTION" -> handleMention(payload);
            case "USER_FOLLOWED" -> handleFollow(payload);
            default -> {
                // ignore other events for now
            }
        }
    }

    private void handleReaction(EventPayload payload) {
        Long reviewId = asLong(payload.metadata(), "reviewId");
        if (reviewId == null || payload.userId() == null) {
            return;
        }
        Review review = loadReviewPort.loadById(reviewId);
        Long recipientId = review.getUserId().value();
        if (recipientId.equals(payload.userId())) {
            return; // self-like skip
        }

        notificationEventUseCase.handle(new NotificationEventPayload(
                recipientId,
                NotificationType.LIKE_ON_REVIEW,
                payload.userId(),
                reviewId,
                NotificationMessages.LIKE_ON_REVIEW
        ));
    }

    private void handleComment(EventPayload payload) {
        Long reviewId = asLong(payload.metadata(), "reviewId");
        if (reviewId == null || payload.userId() == null) {
            return;
        }
        Review review = loadReviewPort.loadById(reviewId);
        Long recipientId = review.getUserId().value();
        if (recipientId.equals(payload.userId())) {
            return; // self-comment skip
        }
        notificationEventUseCase.handle(new NotificationEventPayload(
                recipientId,
                NotificationType.COMMENT_ON_REVIEW,
                payload.userId(),
                reviewId,
                NotificationMessages.COMMENT_ON_REVIEW
        ));
    }

    private void handleMention(EventPayload payload) {
        Long recipientId = asLong(payload.metadata(), "mentionedUserId");
        Long reviewId = asLong(payload.metadata(), "reviewId");
        Long actorId = payload.userId();
        if (recipientId == null || actorId == null) {
            return;
        }
        if (recipientId.equals(actorId)) {
            return;
        }
        notificationEventUseCase.handle(new NotificationEventPayload(
                recipientId,
                NotificationType.MENTION,
                actorId,
                reviewId,
                NotificationMessages.MENTION
        ));
    }

    private void handleFollow(EventPayload payload) {
        Long followeeId = asLong(payload.metadata(), "followeeId");
        Long followerId = payload.userId();
        if (followeeId == null || followerId == null) {
            return;
        }
        if (followeeId.equals(followerId)) {
            return;
        }
        notificationEventUseCase.handle(new NotificationEventPayload(
                followeeId,
                NotificationType.FOLLOWEE_NEW_REVIEW, // reuse enum; add FOLLOWED_YOU if available
                followerId,
                null,
                NotificationMessages.FOLLOWEE_NEW_REVIEW
        ));
    }

    private Long asLong(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
