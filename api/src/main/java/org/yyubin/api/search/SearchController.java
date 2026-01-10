package org.yyubin.api.search;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.book.dto.BookSearchResponse;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.search.dto.ReviewSearchPageResponse;
import org.yyubin.api.search.dto.UnifiedSearchResponse;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.review.search.SearchReviewsUseCase;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.application.review.search.query.SearchReviewsQuery;
import org.yyubin.application.search.service.SearchKeywordTrendingService;
import org.yyubin.application.search.service.SearchQueryNormalizer;
import org.yyubin.domain.search.SearchQuery;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchBooksUseCase searchBooksUseCase;
    private final SearchReviewsUseCase searchReviewsUseCase;
    private final SearchKeywordTrendingService trendingService;
    private final SearchQueryNormalizer queryNormalizer;

    @GetMapping
    public ResponseEntity<UnifiedSearchResponse> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "bookStartIndex", required = false) Integer bookStartIndex,
            @RequestParam(value = "bookSize", required = false) Integer bookSize,
            @RequestParam(value = "reviewCursor", required = false) Long reviewCursor,
            @RequestParam(value = "reviewSize", required = false) Integer reviewSize,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest request
    ) {
        BookSearchPage bookPage = searchBooksUseCase.query(new SearchBooksQuery(
                keyword,
                bookStartIndex,
                bookSize,
                null,
                null,
                null
        ));
        ReviewSearchPageResult reviewPage = searchReviewsUseCase.query(new SearchReviewsQuery(
                keyword,
                reviewCursor,
                reviewSize
        ));

        // Log search query for trending keywords
        logSearchQuery(keyword, bookPage.totalItems() + reviewPage.items().size(), principal, request, "UNIFIED_SEARCH");

        return ResponseEntity.ok(
                UnifiedSearchResponse.of(
                        keyword,
                        BookSearchResponse.from(bookPage),
                        ReviewSearchPageResponse.from(reviewPage)
                )
        );
    }

    private void logSearchQuery(String keyword, long resultCount, Object principal, HttpServletRequest request, String source) {
        try {
            Long userId = PrincipalUtils.resolveUserId(principal);
            String sessionId = request.getSession(false) != null ? request.getSession().getId() : null;
            String normalizedQuery = queryNormalizer.normalize(keyword);

            SearchQuery searchQuery = SearchQuery.of(
                userId,
                sessionId,
                keyword,
                normalizedQuery,
                (int) resultCount,
                source
            );

            trendingService.logSearchQuery(searchQuery);
        } catch (Exception e) {
            // Silent fail - don't break search functionality if logging fails
        }
    }
}
