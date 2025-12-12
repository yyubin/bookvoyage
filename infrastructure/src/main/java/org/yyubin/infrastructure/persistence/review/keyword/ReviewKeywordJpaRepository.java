package org.yyubin.infrastructure.persistence.review.keyword;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewKeywordJpaRepository extends JpaRepository<ReviewKeywordEntity, ReviewKeywordEntity.ReviewKeywordKey> {

    void deleteByIdReviewId(Long reviewId);

    List<ReviewKeywordEntity> findByIdReviewId(Long reviewId);

    List<ReviewKeywordEntity> findByIdReviewIdIn(List<Long> reviewIds);
}
