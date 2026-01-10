package org.yyubin.api.book;

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
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;
import org.yyubin.application.search.service.SearchKeywordTrendingService;
import org.yyubin.application.search.service.SearchQueryNormalizer;
import org.yyubin.domain.search.SearchQuery;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookSearchController {

    private final SearchBooksUseCase searchBooksUseCase;
    private final SearchKeywordTrendingService trendingService;
    private final SearchQueryNormalizer queryNormalizer;

    @GetMapping("/search")
    public ResponseEntity<BookSearchResponse> searchBooks(
            @RequestParam("q") String keyword,
            @RequestParam(value = "startIndex", required = false) Integer startIndex,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "printType", required = false) String printType,
            @AuthenticationPrincipal Object principal,
            HttpServletRequest request
    ) {
        SearchBooksQuery query = new SearchBooksQuery(
                keyword,
                startIndex,
                size,
                language,
                SearchOrder.from(orderBy),
                PrintType.from(printType)
        );

        BookSearchPage page = searchBooksUseCase.query(query);

        // Log search query for trending keywords
        logSearchQuery(keyword, page.totalItems(), principal, request);

        return ResponseEntity.ok(BookSearchResponse.from(page));
    }

    private void logSearchQuery(String keyword, long resultCount, Object principal, HttpServletRequest request) {
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
                "BOOK_SEARCH"
            );

            trendingService.logSearchQuery(searchQuery);
        } catch (Exception e) {
            // Silent fail - don't break search functionality if logging fails
        }
    }
}
