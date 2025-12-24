package org.yyubin.application.userbook;

import org.yyubin.application.userbook.dto.UserBookListResult;
import org.yyubin.application.userbook.query.GetUserBooksQuery;

public interface GetUserBooksUseCase {

    UserBookListResult query(GetUserBooksQuery query);
}
