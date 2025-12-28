package org.yyubin.application.book.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.book.GetBookUseCase;
import org.yyubin.application.book.dto.BookResult;
import org.yyubin.application.book.query.GetBookQuery;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.ReviewCountPort;
import org.yyubin.domain.book.Book;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookQueryService implements GetBookUseCase {

    private final LoadBookPort loadBookPort;
    private final ReviewCountPort reviewCountPort;

    @Override
    public BookResult query(GetBookQuery query) {
        Book book = loadBookPort.loadById(query.bookId())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + query.bookId()));

        long reviewCount = reviewCountPort.countByBookId(query.bookId());
        Double avgRating = reviewCountPort.calculateAverageRating(query.bookId());

        return BookResult.from(book, reviewCount, avgRating);
    }
}
