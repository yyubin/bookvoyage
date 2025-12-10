package org.yyubin.infrastructure.persistence.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.user.port.FollowPort;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.domain.user.UserFollowing;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowPersistenceAdapter implements FollowPort, FollowQueryPort {

    private final UserFollowingJpaRepository userFollowingJpaRepository;

    @Override
    public boolean exists(Long followerId, Long followeeId) {
        return userFollowingJpaRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    @Transactional
    public void create(Long followerId, Long followeeId) {
        UserFollowingEntity entity = UserFollowingEntity.fromDomain(
                UserFollowing.create(new UserId(followerId), new UserId(followeeId))
        );
        userFollowingJpaRepository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long followerId, Long followeeId) {
        userFollowingJpaRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Override
    public List<Long> loadFollowingIds(Long userId, Long cursor, int size) {
        if (cursor != null) {
            return userFollowingJpaRepository.findByFollowerIdAndIdLessThanOrderByIdDesc(userId, cursor, PageRequest.of(0, size))
                    .stream().map(UserFollowingEntity::getFolloweeId).toList();
        }
        return userFollowingJpaRepository.findByFollowerIdOrderByIdDesc(userId, PageRequest.of(0, size))
                .stream().map(UserFollowingEntity::getFolloweeId).toList();
    }

    @Override
    public List<Long> loadFollowerIds(Long userId, Long cursor, int size) {
        if (cursor != null) {
            return userFollowingJpaRepository.findByFolloweeIdAndIdLessThanOrderByIdDesc(userId, cursor, PageRequest.of(0, size))
                    .stream().map(UserFollowingEntity::getFollowerId).toList();
        }
        return userFollowingJpaRepository.findByFolloweeIdOrderByIdDesc(userId, PageRequest.of(0, size))
                .stream().map(UserFollowingEntity::getFollowerId).toList();
    }

    @Override
    public long countFollowing(Long userId) {
        return userFollowingJpaRepository.countByFollowerId(userId);
    }

    @Override
    public long countFollowers(Long userId) {
        return userFollowingJpaRepository.countByFolloweeId(userId);
    }
}
