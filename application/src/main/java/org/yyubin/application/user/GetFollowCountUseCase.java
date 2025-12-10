package org.yyubin.application.user;

import org.yyubin.application.user.dto.FollowCountResult;
import org.yyubin.application.user.query.GetFollowCountQuery;

public interface GetFollowCountUseCase {
    FollowCountResult getCounts(GetFollowCountQuery query);
}
