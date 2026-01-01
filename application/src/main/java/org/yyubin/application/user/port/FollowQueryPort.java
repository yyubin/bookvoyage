package org.yyubin.application.user.port;

import java.util.List;

public interface FollowQueryPort {

    List<Long> loadFollowingIds(Long userId, Long cursor, int size);

    List<Long> loadFollowerIds(Long userId, Long cursor, int size);

    List<Long> loadFollowingIdsAll(Long userId);

    long countFollowing(Long userId);

    long countFollowers(Long userId);

    List<Long> loadFollowerIdsAll(Long userId);
}
