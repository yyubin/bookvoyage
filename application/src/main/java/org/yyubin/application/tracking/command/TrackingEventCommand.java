package org.yyubin.application.tracking.command;

import java.util.Map;

public record TrackingEventCommand(
        String eventId,
        String eventType,
        Long userId,
        String sessionId,
        String deviceId,
        String clientTime,
        String source,
        String contentType,
        String contentId,
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
