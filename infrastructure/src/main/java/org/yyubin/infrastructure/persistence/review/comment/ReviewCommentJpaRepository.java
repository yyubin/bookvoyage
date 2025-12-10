package org.yyubin.infrastructure.persistence.review.comment;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCommentJpaRepository extends JpaRepository<ReviewCommentEntity, Long> {

    Optional<ReviewCommentEntity> findByIdAndDeletedFalse(Long id);

    List<ReviewCommentEntity> findByReviewIdAndDeletedFalseOrderByIdDesc(Long reviewId, Pageable pageable);

    List<ReviewCommentEntity> findByReviewIdAndDeletedFalseAndIdLessThanOrderByIdDesc(Long reviewId, Long id, Pageable pageable);
}
