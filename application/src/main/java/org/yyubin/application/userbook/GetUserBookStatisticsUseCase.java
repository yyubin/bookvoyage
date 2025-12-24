package org.yyubin.application.userbook;

import org.yyubin.application.userbook.dto.UserBookStatisticsResult;
import org.yyubin.application.userbook.query.GetUserBookStatisticsQuery;

public interface GetUserBookStatisticsUseCase {

    UserBookStatisticsResult query(GetUserBookStatisticsQuery query);
}
