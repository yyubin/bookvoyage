package org.yyubin.application.activity.port;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.domain.activity.ActivityItem;

public interface ActivityQueryPort {
    List<ActivityItem> loadActivities(List<Long> followingIds, Long userId, LocalDateTime cursor, int size);
}
