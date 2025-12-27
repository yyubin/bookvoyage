package org.yyubin.api.review;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.search.dto.ReviewSearchPageResponse;
import org.yyubin.application.review.search.SearchReviewsUseCase;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.application.review.search.query.ReviewSortOption;
import org.yyubin.application.review.search.query.SearchReviewsQuery;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewSearchController {

    private final SearchReviewsUseCase searchReviewsUseCase;

    @GetMapping("/search")
    public ResponseEntity<ReviewSearchPageResponse> searchReviews(
            @RequestParam("q") String keyword,

            // 페이지네이션
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "size", required = false) Integer size,

            // 필터링 옵션
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "minRating", required = false) Integer minRating,
            @RequestParam(value = "maxRating", required = false) Integer maxRating,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "highlight", required = false) String highlight,
            @RequestParam(value = "bookId", required = false) Long bookId,
            @RequestParam(value = "userId", required = false) Long userId,

            // 정렬 옵션
            @RequestParam(value = "sortBy", required = false) String sortBy
    ) {
        SearchReviewsQuery query = new SearchReviewsQuery(
                keyword,
                cursor,
                size,
                genre,
                minRating,
                maxRating,
                startDate,
                endDate,
                highlight,
                bookId,
                userId,
                ReviewSortOption.from(sortBy)
        );

        ReviewSearchPageResult result = searchReviewsUseCase.query(query);
        return ResponseEntity.ok(ReviewSearchPageResponse.from(result));
    }
}
