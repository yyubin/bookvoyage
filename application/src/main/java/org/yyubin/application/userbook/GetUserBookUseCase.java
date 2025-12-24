package org.yyubin.application.userbook;

import org.yyubin.application.userbook.dto.UserBookResult;
import org.yyubin.application.userbook.query.GetUserBookQuery;

public interface GetUserBookUseCase {

    UserBookResult query(GetUserBookQuery query);
}
