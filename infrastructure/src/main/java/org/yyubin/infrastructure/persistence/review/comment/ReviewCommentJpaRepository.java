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

    long countByReviewIdAndDeletedFalse(Long reviewId);

    List<ReviewCommentEntity> findByParentCommentIdAndDeletedFalseOrderByIdDesc(Long parentCommentId, Pageable pageable);

    List<ReviewCommentEntity> findByParentCommentIdAndDeletedFalseAndIdLessThanOrderByIdDesc(Long parentCommentId, Long id, Pageable pageable);

    long countByParentCommentIdAndDeletedFalse(Long parentCommentId);

    @org.springframework.data.jpa.repository.Query("SELECT c.parentCommentId as parentId, COUNT(c) as count FROM ReviewCommentEntity c WHERE c.parentCommentId IN :parentIds AND c.deleted = false GROUP BY c.parentCommentId")
    List<ParentCommentCount> countRepliesByParentIds(List<Long> parentIds);

    interface ParentCommentCount {
        Long getParentId();
        Long getCount();
    }
}
