package org.yyubin.application.review.command;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewTrackingCommand {
    public enum EventType {
        CLICK,
        SCROLL,
        DWELL,
        REACH
    }

    private final Long reviewId;
    private final Long bookId;
    private final Long userId;
    private final EventType eventType;
    private final Double position;
    private final Double depthPct;
    private final Long dwellMs;
    private final String source;
    private final Map<String, Object> metadata;
}
