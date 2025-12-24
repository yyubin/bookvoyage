package org.yyubin.api.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.review.dto.ReviewTrackingRequest;
import org.yyubin.application.review.TrackReviewEventUseCase;
import org.yyubin.application.review.command.ReviewTrackingCommand;

@RestController
@RequestMapping("/api/reviews/{reviewId}/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackReviewEventUseCase trackReviewEventUseCase;

    @PostMapping
    public ResponseEntity<Void> track(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewTrackingRequest request,
            @AuthenticationPrincipal Object principal
    ) {
        Long userId = PrincipalUtils.resolveUserId(principal);
        trackReviewEventUseCase.track(ReviewTrackingCommand.builder()
                .reviewId(reviewId)
                .bookId(request.bookId())
                .userId(userId)
                .eventType(ReviewTrackingCommand.EventType.valueOf(request.eventType()))
                .position(request.position())
                .depthPct(request.depthPct())
                .dwellMs(request.dwellMs())
                .source(request.source())
                .metadata(request.metadata())
                .build());
        return ResponseEntity.accepted().build();
    }

}
