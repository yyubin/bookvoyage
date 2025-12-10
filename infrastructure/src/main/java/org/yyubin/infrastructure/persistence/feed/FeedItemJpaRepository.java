package org.yyubin.infrastructure.persistence.feed;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedItemJpaRepository extends JpaRepository<FeedItemEntity, Long> {

    List<FeedItemEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<FeedItemEntity> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT f FROM FeedItemEntity f WHERE f.userId = :userId AND f.createdAt < :cursor ORDER BY f.createdAt DESC")
    List<FeedItemEntity> findByUserIdAndCreatedAtBeforeOrderByCreatedAtDesc(Long userId, LocalDateTime cursor, Pageable pageable);

    List<FeedItemEntity> findByReviewId(Long reviewId);

    void deleteByReviewId(Long reviewId);

    void deleteByUserId(Long userId);
}
