package org.yyubin.application.user;

import org.yyubin.application.user.dto.FollowPageResult;
import org.yyubin.application.user.query.GetFollowerUsersQuery;

public interface GetFollowerUsersUseCase {
    FollowPageResult getFollowers(GetFollowerUsersQuery query);
}
