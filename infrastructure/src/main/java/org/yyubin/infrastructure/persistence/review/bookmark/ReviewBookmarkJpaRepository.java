package org.yyubin.infrastructure.persistence.review.bookmark;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewBookmarkJpaRepository extends JpaRepository<ReviewBookmarkEntity, Long> {

    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);

    void deleteByUserIdAndReviewId(Long userId, Long reviewId);

    List<ReviewBookmarkEntity> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long cursorId);

    List<ReviewBookmarkEntity> findByUserIdOrderByIdDesc(Long userId);

    java.util.Optional<ReviewBookmarkEntity> findByUserIdAndReviewId(Long userId, Long reviewId);

    long countByReviewId(Long reviewId);

    long countByUserId(Long userId);
}
