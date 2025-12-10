package org.yyubin.domain.user;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a follow relationship between users
 */
@Getter
@ToString
@EqualsAndHashCode(of = "id")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UserFollowing {
    private final Long id;
    private final UserId followerId;
    private final UserId followeeId;
    private final LocalDateTime createdAt;

    public static UserFollowing of(Long id, UserId followerId, UserId followeeId, LocalDateTime createdAt) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        Objects.requireNonNull(followerId, "Follower ID cannot be null");
        Objects.requireNonNull(followeeId, "Followee ID cannot be null");
        Objects.requireNonNull(createdAt, "Created at cannot be null");

        return new UserFollowing(id, followerId, followeeId, createdAt);
    }

    public static UserFollowing create(UserId followerId, UserId followeeId) {
        return of(null, followerId, followeeId, LocalDateTime.now());
    }
}
