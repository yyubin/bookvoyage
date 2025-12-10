package org.yyubin.infrastructure.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserFollowingJpaRepository extends JpaRepository<UserFollowingEntity, Long> {

    List<UserFollowingEntity> findByFollowerId(Long followerId);

    List<UserFollowingEntity> findByFolloweeId(Long followeeId);

    Optional<UserFollowingEntity> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    List<UserFollowingEntity> findByFollowerIdOrderByIdDesc(Long followerId, Pageable pageable);

    List<UserFollowingEntity> findByFollowerIdAndIdLessThanOrderByIdDesc(Long followerId, Long id, Pageable pageable);

    List<UserFollowingEntity> findByFolloweeIdOrderByIdDesc(Long followeeId, Pageable pageable);

    List<UserFollowingEntity> findByFolloweeIdAndIdLessThanOrderByIdDesc(Long followeeId, Long id, Pageable pageable);

    long countByFollowerId(Long followerId);

    long countByFolloweeId(Long followeeId);
}
