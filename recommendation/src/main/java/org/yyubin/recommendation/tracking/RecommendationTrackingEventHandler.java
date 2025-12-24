package org.yyubin.recommendation.tracking;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.domain.review.Review;
import org.yyubin.recommendation.config.RecommendationTrackingProperties;
import org.yyubin.recommendation.service.RecommendationCacheService;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationTrackingEventHandler {

    private final RecommendationCacheService cacheService;
    private final LoadReviewPort loadReviewPort;
    private final RecommendationTrackingProperties properties;

    public void handle(EventPayload payload) {
        if (payload.userId() == null) {
            return;
        }
        String eventType = payload.eventType();
        if (eventType == null) {
            return;
        }

        String contentType = payload.targetType();
        String contentId = payload.targetId();
        Map<String, Object> metadata = payload.metadata();

        if (metadata != null) {
            contentType = asString(metadata.getOrDefault("contentType", contentType));
            contentId = asString(metadata.getOrDefault("contentId", contentId));
        }

        if (contentType == null || contentId == null) {
            return;
        }

        Long bookId = resolveBookId(contentType, contentId);
        if (bookId == null) {
            return;
        }

        double delta = weight(eventType, metadata);
        if (delta == 0) {
            return;
        }

        cacheService.incrementBookScore(payload.userId(), bookId, delta);
    }

    private Long resolveBookId(String contentType, String contentId) {
        try {
            return switch (contentType) {
                case "BOOK" -> Long.parseLong(contentId);
                case "REVIEW" -> {
                    Review review = loadReviewPort.loadById(Long.parseLong(contentId));
                    yield review.getBookId().getValue();
                }
                default -> null;
            };
        } catch (Exception ex) {
            log.debug("Failed to resolve bookId from contentType={} contentId={}", contentType, contentId);
            return null;
        }
    }

    private double weight(String eventType, Map<String, Object> metadata) {
        RecommendationTrackingProperties.Weights weights = properties.getWeights();
        RecommendationTrackingProperties.Caps caps = properties.getCaps();

        return switch (eventType) {
            case "IMPRESSION" -> weights.getImpression();
            case "CLICK" -> weights.getClick();
            case "DWELL" -> Math.min(caps.getDwellMax(), weights.getDwellPerMs() * asDouble(metadata, "dwellMs"));
            case "SCROLL" -> Math.min(caps.getScrollMax(), weights.getScrollPerPct() * asDouble(metadata, "scrollDepthPct"));
            case "BOOKMARK" -> weights.getBookmark();
            case "LIKE" -> weights.getLike();
            case "FOLLOW" -> weights.getFollow();
            case "REVIEW_CREATE" -> weights.getReviewCreate();
            case "REVIEW_UPDATE" -> weights.getReviewUpdate();
            default -> 0.0;
        };
    }

    private double asDouble(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return 0.0;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException ignored) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private String asString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
