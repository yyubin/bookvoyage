package org.yyubin.api.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.book.dto.BookSearchResponse;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookSearchController {

    private final SearchBooksUseCase searchBooksUseCase;

    @GetMapping("/search")
    public ResponseEntity<BookSearchResponse> searchBooks(
            @RequestParam("q") String keyword,
            @RequestParam(value = "startIndex", required = false) Integer startIndex,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "printType", required = false) String printType
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
        return ResponseEntity.ok(BookSearchResponse.from(page));
    }
}
