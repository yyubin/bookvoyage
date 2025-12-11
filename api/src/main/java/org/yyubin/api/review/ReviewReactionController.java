package org.yyubin.api.review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.review.dto.ReactionRequest;
import org.yyubin.api.review.dto.ReactionResponse;
import org.yyubin.application.review.DeleteReactionUseCase;
import org.yyubin.application.review.dto.ReviewReactionResult;
import org.yyubin.application.review.UpsertReactionUseCase;
import org.yyubin.application.review.command.DeleteReactionCommand;
import org.yyubin.application.review.command.UpsertReactionCommand;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewReactionController {

    private final UpsertReactionUseCase upsertReactionUseCase;
    private final DeleteReactionUseCase deleteReactionUseCase;

    @PutMapping("/{reviewId}/reaction")
    public ResponseEntity<ReactionResponse> upsertReaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId,
            @Valid @RequestBody ReactionRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReviewReactionResult result = upsertReactionUseCase.execute(
                new UpsertReactionCommand(reviewId, userId, request.content())
        );
        return ResponseEntity.ok(ReactionResponse.from(result));
    }

    @DeleteMapping("/{reviewId}/reaction")
    public ResponseEntity<Void> deleteReaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        deleteReactionUseCase.execute(new DeleteReactionCommand(reviewId, userId));
        return ResponseEntity.noContent().build();
    }
}
