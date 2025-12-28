package org.yyubin.api.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.review.dto.ReviewLikeResponse;
import org.yyubin.application.review.ToggleReviewLikeUseCase;
import org.yyubin.application.review.command.ToggleReviewLikeCommand;

@RestController
@RequestMapping("/api/reviews/{reviewId}/like")
@RequiredArgsConstructor
public class ReviewLikeController {

    private final ToggleReviewLikeUseCase toggleReviewLikeUseCase;

    @PostMapping
    public ResponseEntity<ReviewLikeResponse> toggleLike(
            @AuthenticationPrincipal Object principal,
            @PathVariable Long reviewId
    ) {
        Long userId = PrincipalUtils.requireUserId(principal);

        ToggleReviewLikeUseCase.ToggleResult result = toggleReviewLikeUseCase.execute(
                new ToggleReviewLikeCommand(reviewId, userId)
        );

        return ResponseEntity.ok(new ReviewLikeResponse(result.isLiked(), result.likeCount()));
    }
}
