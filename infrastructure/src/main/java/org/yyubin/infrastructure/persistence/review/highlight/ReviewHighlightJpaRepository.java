package org.yyubin.infrastructure.persistence.review.highlight;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewHighlightJpaRepository extends JpaRepository<ReviewHighlightEntity, ReviewHighlightEntity.ReviewHighlightKey> {

    void deleteByIdReviewId(Long reviewId);

    List<ReviewHighlightEntity> findByIdReviewId(Long reviewId);

    List<ReviewHighlightEntity> findByIdReviewIdIn(List<Long> reviewIds);
}
