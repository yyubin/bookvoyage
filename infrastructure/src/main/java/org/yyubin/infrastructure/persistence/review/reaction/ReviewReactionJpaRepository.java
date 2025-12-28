package org.yyubin.infrastructure.persistence.review.reaction;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReactionJpaRepository extends JpaRepository<ReviewReactionEntity, Long> {

    Optional<ReviewReactionEntity> findByReviewIdAndUserId(Long reviewId, Long userId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    java.util.List<ReviewReactionEntity> findByUserId(Long userId);

    long countByReviewId(Long reviewId);

    @Query("SELECT r.content as emoji, COUNT(r) as count FROM ReviewReactionEntity r WHERE r.reviewId = :reviewId GROUP BY r.content")
    java.util.List<ReactionCountProjection> countByReviewIdGroupByContent(Long reviewId);

    interface ReactionCountProjection {
        String getEmoji();
        Long getCount();
    }
}
