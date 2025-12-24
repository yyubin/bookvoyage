package org.yyubin.api.tracking;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.tracking.dto.TrackingEventRequest;
import org.yyubin.api.tracking.dto.TrackingEventsRequest;
import org.yyubin.application.tracking.TrackRecommendationEventsUseCase;
import org.yyubin.application.tracking.command.TrackRecommendationEventsCommand;
import org.yyubin.application.tracking.command.TrackingEventCommand;

@RestController
@RequestMapping("/api/tracking/events")
@RequiredArgsConstructor
public class TrackingEventsController {

    private final TrackRecommendationEventsUseCase trackRecommendationEventsUseCase;

    @PostMapping
    public ResponseEntity<Void> trackBatch(
            @Valid @RequestBody TrackingEventsRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        Long userId = PrincipalUtils.resolveUserId(principal);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(
                request.events().stream().map(event -> toCommand(event, userId)).toList()
        );
        trackRecommendationEventsUseCase.track(command);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/single")
    public ResponseEntity<Void> trackSingle(
            @Valid @RequestBody TrackingEventRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        Long userId = PrincipalUtils.resolveUserId(principal);
        TrackRecommendationEventsCommand command = new TrackRecommendationEventsCommand(
                List.of(toCommand(request, userId))
        );
        trackRecommendationEventsUseCase.track(command);
        return ResponseEntity.accepted().build();
    }

    private TrackingEventCommand toCommand(TrackingEventRequest request, Long userId) {
        return new TrackingEventCommand(
                request.eventId(),
                request.eventType(),
                userId,
                request.sessionId(),
                request.deviceId(),
                request.clientTime(),
                request.source(),
                request.contentType(),
                request.contentId(),
                request.position(),
                request.rank(),
                request.score(),
                request.requestId(),
                request.algorithm(),
                request.dwellMs(),
                request.scrollDepthPct(),
                request.visibilityMs(),
                request.metadata()
        );
    }

}
