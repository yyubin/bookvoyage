package org.yyubin.application.book;

import org.yyubin.application.book.dto.BookResult;
import org.yyubin.application.book.query.GetBookQuery;

public interface GetBookUseCase {
    BookResult query(GetBookQuery query);
}
