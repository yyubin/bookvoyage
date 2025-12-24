package org.yyubin.application.userbook;

import org.yyubin.application.userbook.dto.UserBookListResult;
import org.yyubin.application.userbook.query.GetLatestReadingBooksQuery;

public interface GetLatestReadingBooksUseCase {
    UserBookListResult query(GetLatestReadingBooksQuery query);
}
