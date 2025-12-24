package org.yyubin.api.search;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.book.dto.BookSearchResponse;
import org.yyubin.api.search.dto.ReviewSearchPageResponse;
import org.yyubin.api.search.dto.UnifiedSearchResponse;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.review.search.SearchReviewsUseCase;
import org.yyubin.application.review.search.dto.ReviewSearchPageResult;
import org.yyubin.application.review.search.query.SearchReviewsQuery;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchBooksUseCase searchBooksUseCase;
    private final SearchReviewsUseCase searchReviewsUseCase;

    @GetMapping
    public ResponseEntity<UnifiedSearchResponse> search(
            @RequestParam("q") String keyword,
            @RequestParam(value = "bookStartIndex", required = false) Integer bookStartIndex,
            @RequestParam(value = "bookSize", required = false) Integer bookSize,
            @RequestParam(value = "reviewCursor", required = false) Long reviewCursor,
            @RequestParam(value = "reviewSize", required = false) Integer reviewSize
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
        return ResponseEntity.ok(
                UnifiedSearchResponse.of(
                        keyword,
                        BookSearchResponse.from(bookPage),
                        ReviewSearchPageResponse.from(reviewPage)
                )
        );
    }
}
