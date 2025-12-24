package org.yyubin.application.tracking;

import org.yyubin.application.tracking.command.TrackRecommendationEventsCommand;

public interface TrackRecommendationEventsUseCase {
    void track(TrackRecommendationEventsCommand command);
}
