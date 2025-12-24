package org.yyubin.api.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record TrackingEventRequest(
        @NotBlank String eventId,
        @NotBlank String eventType,
        String sessionId,
        String deviceId,
        String clientTime,
        String source,
        @NotBlank String contentType,
        @NotBlank String contentId,
        Integer position,
        Integer rank,
        Double score,
        String requestId,
        String algorithm,
        Long dwellMs,
        Double scrollDepthPct,
        Long visibilityMs,
        Map<String, Object> metadata
) {
}
