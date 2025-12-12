package org.yyubin.application.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.review.TrackReviewEventUseCase;
import org.yyubin.application.review.command.ReviewTrackingCommand;

@Service
@RequiredArgsConstructor
public class ReviewTrackingService implements TrackReviewEventUseCase {

    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public void track(ReviewTrackingCommand command) {
        if (command.getReviewId() == null) {
            throw new IllegalArgumentException("reviewId is required");
        }

        java.util.Map<String, Object> meta = new java.util.HashMap<>();
        meta.put("reviewId", command.getReviewId());
        if (command.getBookId() != null) meta.put("bookId", command.getBookId());
        if (command.getPosition() != null) meta.put("position", command.getPosition());
        if (command.getDepthPct() != null) meta.put("depthPct", command.getDepthPct());
        if (command.getDwellMs() != null) meta.put("dwellMs", command.getDwellMs());
        if (command.getSource() != null) meta.put("source", command.getSource());
        if (command.getMetadata() != null) meta.putAll(command.getMetadata());

        String eventType = switch (command.getEventType()) {
            case CLICK -> "REVIEW_CLICKED";
            case SCROLL -> "REVIEW_SCROLLED";
            case DWELL -> "REVIEW_DWELL";
            case REACH -> "REVIEW_REACHED";
        };

        eventPublisher.publish(
                EventTopics.REVIEW,
                command.getUserId() != null ? command.getUserId().toString() : "anonymous",
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        eventType,
                        command.getUserId(),
                        "REVIEW",
                        command.getReviewId().toString(),
                        meta,
                        java.time.Instant.now(),
                        "tracking-api",
                        1
                )
        );
    }
}
