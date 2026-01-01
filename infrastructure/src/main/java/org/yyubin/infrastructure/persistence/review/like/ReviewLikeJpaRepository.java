package org.yyubin.infrastructure.persistence.review.like;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeJpaRepository extends JpaRepository<ReviewLikeEntity, Long> {

    Optional<ReviewLikeEntity> findByReviewIdAndUserId(Long reviewId, Long userId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);

    long countByReviewId(Long reviewId);

    List<ReviewLikeEntity> findByUserIdInOrderByCreatedAtDesc(List<Long> userIds, org.springframework.data.domain.Pageable pageable);

    List<ReviewLikeEntity> findByUserIdInAndCreatedAtBeforeOrderByCreatedAtDesc(
            List<Long> userIds,
            java.time.LocalDateTime cursor,
            org.springframework.data.domain.Pageable pageable
    );

    @Query("SELECT l.reviewId as reviewId, COUNT(l) as count FROM ReviewLikeEntity l WHERE l.reviewId IN :reviewIds GROUP BY l.reviewId")
    List<ReviewLikeCount> countByReviewIds(List<Long> reviewIds);

    interface ReviewLikeCount {
        Long getReviewId();
        Long getCount();
    }
}
