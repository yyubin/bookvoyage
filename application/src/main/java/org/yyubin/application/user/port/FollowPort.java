package org.yyubin.application.user.port;

public interface FollowPort {
    boolean exists(Long followerId, Long followeeId);

    void create(Long followerId, Long followeeId);

    void delete(Long followerId, Long followeeId);
}
