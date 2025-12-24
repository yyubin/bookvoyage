package org.yyubin.application.tracking.command;

import java.util.List;

public record TrackRecommendationEventsCommand(
        List<TrackingEventCommand> events
) {
    public TrackRecommendationEventsCommand {
        if (events == null || events.isEmpty()) {
            throw new IllegalArgumentException("events must not be empty");
        }
    }
}
