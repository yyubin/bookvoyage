package org.yyubin.api.book;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.book.dto.BookResponse;
import org.yyubin.api.book.dto.BookReviewsResponse;
import org.yyubin.application.book.GetBookUseCase;
import org.yyubin.application.book.GetBookReviewsUseCase;
import org.yyubin.application.book.dto.BookResult;
import org.yyubin.application.book.dto.BookReviewsResult;
import org.yyubin.application.book.query.GetBookQuery;
import org.yyubin.application.book.query.GetBookReviewsQuery;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final GetBookUseCase getBookUseCase;
    private final GetBookReviewsUseCase getBookReviewsUseCase;

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long bookId) {
        BookResult result = getBookUseCase.query(new GetBookQuery(bookId));
        return ResponseEntity.ok(BookResponse.from(result));
    }

    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<BookReviewsResponse> getBookReviews(
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "recommended") String sort
    ) {
        GetBookReviewsQuery query = new GetBookReviewsQuery(bookId, cursor, size, sort);
        BookReviewsResult result = getBookReviewsUseCase.query(query);
        return ResponseEntity.ok(BookReviewsResponse.from(result));
    }
}
