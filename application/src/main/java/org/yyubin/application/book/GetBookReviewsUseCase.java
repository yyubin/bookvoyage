package org.yyubin.application.book;

import org.yyubin.application.book.dto.BookReviewsResult;
import org.yyubin.application.book.query.GetBookReviewsQuery;

public interface GetBookReviewsUseCase {
    BookReviewsResult query(GetBookReviewsQuery query);
}
