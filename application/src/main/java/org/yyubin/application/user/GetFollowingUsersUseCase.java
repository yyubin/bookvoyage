package org.yyubin.application.user;

import org.yyubin.application.user.dto.FollowPageResult;
import org.yyubin.application.user.query.GetFollowingUsersQuery;

public interface GetFollowingUsersUseCase {
    FollowPageResult getFollowing(GetFollowingUsersQuery query);
}
