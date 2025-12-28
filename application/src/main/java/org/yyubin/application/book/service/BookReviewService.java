package org.yyubin.application.book.service;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.book.GetBookReviewsUseCase;
import org.yyubin.application.book.dto.BookReviewsResult;
import org.yyubin.application.book.port.SearchBookReviewsPort;
import org.yyubin.application.book.query.GetBookReviewsQuery;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookReviewService implements GetBookReviewsUseCase {

    private final SearchBookReviewsPort searchBookReviewsPort;

    @Override
    public BookReviewsResult query(GetBookReviewsQuery query) {
        SearchBookReviewsPort.SearchResult searchResult = searchBookReviewsPort.searchByBookId(
                query.bookId(),
                query.cursor(),
                query.size(),
                query.sort()
        );

        var reviews = searchResult.reviews().stream()
                .map(doc -> new BookReviewsResult.ReviewSummary(
                        doc.reviewId(),
                        doc.userId(),
                        doc.title(),
                        doc.rating(),
                        doc.content(),
                        doc.createdAt(),
                        doc.likeCount(),
                        doc.commentCount(),
                        doc.viewCount()
                ))
                .collect(Collectors.toList());

        return new BookReviewsResult(
                reviews,
                searchResult.nextCursor(),
                searchResult.totalCount()
        );
    }
}
