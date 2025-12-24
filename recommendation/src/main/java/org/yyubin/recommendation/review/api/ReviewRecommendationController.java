package org.yyubin.recommendation.review.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.recommendation.review.HighlightRecommendationResult;
import org.yyubin.recommendation.review.ReviewRecommendationService;

@RestController
@RequestMapping("/recommendations/reviews")
@RequiredArgsConstructor
public class ReviewRecommendationController {

    private final ReviewRecommendationService reviewRecommendationService;

    @GetMapping("/highlights")
    public ResponseEntity<HighlightRecommendationResult> recommendByHighlight(
            @RequestParam("highlight") String highlight,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                reviewRecommendationService.recommendByHighlight(highlight, cursor, size)
        );
    }
}
