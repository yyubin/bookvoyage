package org.yyubin.recommendation.review.event;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.recommendation.review.RecommendationIngestCommand;
import org.yyubin.recommendation.review.ReviewRecommendationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReviewRecommendationEventHandler {

    private final ReviewRecommendationService reviewRecommendationService;
    private final HighlightNormalizer highlightNormalizer;

    @Async("recommendationTaskExecutor")
    public void handle(EventPayload payload) {
        if (payload == null || payload.eventType() == null) {
            return;
        }
        switch (payload.eventType()) {
            case "REVIEW_CREATED", "REVIEW_UPDATED" -> ingest(payload);
            case "REVIEW_DELETED" -> delete(payload);
            default -> {
                // ignore other events
            }
        }
    }

    private void ingest(EventPayload payload) {
        Map<String, Object> metadata = payload.metadata();
        if (metadata == null) {
            return;
        }

        Long reviewId = asLong(metadata.get("reviewId"));
        Long userId = payload.userId();
        Long bookId = asLong(metadata.get("bookId"));
        if (reviewId == null || userId == null || bookId == null) {
            log.debug("Skip ingest due to missing ids reviewId={} userId={} bookId={}", reviewId, userId, bookId);
            return;
        }

        List<String> highlights = asStringList(metadata.get("highlights"));
        List<String> highlightsNorm = asStringList(metadata.get("highlightsNorm"));
        if (highlightsNorm.isEmpty() && !highlights.isEmpty()) {
            highlightsNorm = highlights.stream()
                    .filter(value -> value != null && !value.isBlank())
                    .map(highlightNormalizer::normalize)
                    .toList();
        }

        RecommendationIngestCommand command = new RecommendationIngestCommand(
                reviewId,
                userId,
                bookId,
                asString(metadata.get("summary")),
                asString(metadata.get("content")),
                highlights,
                highlightsNorm,
                asStringList(metadata.get("keywords")),
                asString(metadata.get("genre")),
                asLocalDateTime(metadata.get("createdAt")),
                asInteger(metadata.get("rating"))
        );

        reviewRecommendationService.ingest(command);
    }

    private void delete(EventPayload payload) {
        Map<String, Object> metadata = payload.metadata();
        Long reviewId = metadata != null ? asLong(metadata.get("reviewId")) : null;
        if (reviewId != null) {
            reviewRecommendationService.deleteReview(reviewId);
        }
    }

    private List<String> asStringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> results = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                results.add(item.toString());
            }
        }
        return results;
    }

    private String asString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value instanceof String text) {
            try {
                return LocalDateTime.parse(text);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }
}
