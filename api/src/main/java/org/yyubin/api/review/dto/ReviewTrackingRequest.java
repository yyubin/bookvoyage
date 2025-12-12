package org.yyubin.api.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record ReviewTrackingRequest(
        Long bookId,
        @NotBlank String eventType, // CLICK, SCROLL, DWELL, REACH
        Double position,
        Double depthPct,
        Long dwellMs,
        String source,
        Map<String, Object> metadata
) { }
