package org.yyubin.application.activity;

import org.yyubin.application.activity.dto.ActivityFeedPageResult;
import org.yyubin.application.activity.query.GetActivityFeedQuery;

public interface GetActivityFeedUseCase {
    ActivityFeedPageResult query(GetActivityFeedQuery query);
}
