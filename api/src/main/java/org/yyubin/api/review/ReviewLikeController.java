package org.yyubin.api.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
            @PathVariable Long reviewId,
            @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        ToggleReviewLikeUseCase.ToggleResult result = toggleReviewLikeUseCase.execute(
                new ToggleReviewLikeCommand(reviewId, userId)
        );

        return ResponseEntity.ok(new ReviewLikeResponse(result.isLiked(), result.likeCount()));
    }
}
