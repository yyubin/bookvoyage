package org.yyubin.application.review;

import org.yyubin.application.review.command.ReviewTrackingCommand;

public interface TrackReviewEventUseCase {
    void track(ReviewTrackingCommand command);
}
