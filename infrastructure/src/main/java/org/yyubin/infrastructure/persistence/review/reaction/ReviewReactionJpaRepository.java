package org.yyubin.infrastructure.persistence.review.reaction;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReactionJpaRepository extends JpaRepository<ReviewReactionEntity, Long> {

    Optional<ReviewReactionEntity> findByReviewIdAndUserId(Long reviewId, Long userId);

    void deleteByReviewIdAndUserId(Long reviewId, Long userId);

    java.util.List<ReviewReactionEntity> findByUserId(Long userId);

    long countByReviewId(Long reviewId);
}
