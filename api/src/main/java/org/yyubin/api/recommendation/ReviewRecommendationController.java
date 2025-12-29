package org.yyubin.api.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.recommendation.dto.ReviewRecommendationResponse;
import org.yyubin.application.recommendation.GetReviewRecommendationsUseCase;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;
import org.yyubin.application.recommendation.query.GetReviewRecommendationsQuery;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations/reviews")
@RequiredArgsConstructor
public class ReviewRecommendationController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final GetReviewRecommendationsUseCase getReviewRecommendationsUseCase;

    @GetMapping
    public ResponseEntity<ReviewRecommendationResponse> getReviewRecommendations(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "limit", required = false) @Min(1) @Max(MAX_LIMIT) Integer limit,
            @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh
    ) {
        // 비로그인 사용자도 접근 가능 (userId = null)
        Long userId = PrincipalUtils.resolveUserId(principal);
        int requestLimit = limit == null ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        GetReviewRecommendationsQuery query = new GetReviewRecommendationsQuery(
                userId,
                cursor,
                requestLimit,
                forceRefresh
        );

        List<ReviewRecommendationResultDto> results = getReviewRecommendationsUseCase.query(query);

        return ResponseEntity.ok(ReviewRecommendationResponse.from(results, requestLimit));
    }
}
