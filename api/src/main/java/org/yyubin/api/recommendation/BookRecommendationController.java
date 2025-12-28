package org.yyubin.api.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.recommendation.dto.BookRecommendationResponse;
import org.yyubin.application.recommendation.GetBookRecommendationsUseCase;
import org.yyubin.application.recommendation.dto.BookRecommendationResult;
import org.yyubin.application.recommendation.query.GetBookRecommendationsQuery;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations/books")
@RequiredArgsConstructor
public class BookRecommendationController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    private final GetBookRecommendationsUseCase getBookRecommendationsUseCase;

    @GetMapping
    public ResponseEntity<BookRecommendationResponse> getBookRecommendations(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "limit", required = false) @Min(1) @Max(MAX_LIMIT) Integer limit,
            @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh
    ) {
        // 비로그인 사용자도 접근 가능 (userId = null)
        Long userId = PrincipalUtils.resolveUserId(principal);
        int requestLimit = limit == null ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        GetBookRecommendationsQuery query = new GetBookRecommendationsQuery(
                userId,
                requestLimit,
                forceRefresh
        );

        List<BookRecommendationResult> results = getBookRecommendationsUseCase.query(query);

        return ResponseEntity.ok(BookRecommendationResponse.from(results));
    }
}
