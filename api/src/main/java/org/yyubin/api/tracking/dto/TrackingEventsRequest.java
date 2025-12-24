package org.yyubin.api.tracking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record TrackingEventsRequest(
        @NotEmpty List<@Valid TrackingEventRequest> events
) {
}
