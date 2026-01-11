package org.yyubin.infrastructure.persistence.review;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.yyubin.domain.review.ReviewVisibility;
import org.springframework.data.repository.query.Param;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {

    /**
     * N+1 쿼리 방지: 리뷰 상세 조회 시 user를 함께 fetch
     * comments와 reactions는 별도 쿼리로 조회 (OneToMany는 EntityGraph에 포함하지 않는 것이 좋음)
     */
    @EntityGraph(attributePaths = {"user"})
    Optional<ReviewEntity> findWithUserById(Long id);

    List<ReviewEntity> findByUserId(Long userId);

    List<ReviewEntity> findByBookId(Long bookId);

    List<ReviewEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ReviewEntity> findByBookIdOrderByCreatedAtDesc(Long bookId);

    @Query("SELECT r FROM ReviewEntity r WHERE r.userId = :userId AND r.bookId = :bookId")
    List<ReviewEntity> findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.bookId = :bookId")
    Double calculateAverageRating(Long bookId);

    long countByBookId(Long bookId);

    long countByUserIdAndDeletedFalse(Long userId);

    List<ReviewEntity> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    List<ReviewEntity> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long id, Pageable pageable);

    List<ReviewEntity> findByUserIdAndDeletedFalseOrderByIdDesc(Long userId, Pageable pageable);

    List<ReviewEntity> findByUserIdAndDeletedFalseAndIdLessThanOrderByIdDesc(Long userId, Long id, Pageable pageable);

    List<ReviewEntity> findByUserIdAndDeletedFalseAndVisibilityOrderByIdDesc(Long userId, ReviewVisibility visibility, Pageable pageable);

    List<ReviewEntity> findByUserIdAndDeletedFalseAndVisibilityAndIdLessThanOrderByIdDesc(Long userId, ReviewVisibility visibility, Long id, Pageable pageable);

    List<ReviewEntity> findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(
            List<Long> userIds,
            ReviewVisibility visibility,
            Pageable pageable
    );

    List<ReviewEntity> findByUserIdInAndDeletedFalseAndVisibilityAndCreatedAtBeforeOrderByCreatedAtDesc(
            List<Long> userIds,
            ReviewVisibility visibility,
            java.time.LocalDateTime cursor,
            Pageable pageable
    );

    Slice<ReviewEntity> findByUpdatedAtAfterOrderByIdAsc(java.time.LocalDateTime updatedAt, Pageable pageable);

    @Query(value = """
            SELECT r.* FROM review r
            JOIN review_highlight rh ON r.id = rh.review_id
            JOIN highlight h ON rh.highlight_id = h.id
            WHERE h.normalized_value = :normalized
              AND r.is_deleted = false
              AND r.visibility = 'PUBLIC'
            ORDER BY r.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ReviewEntity> findByHighlightNormalized(@Param("normalized") String normalized, @Param("limit") int limit);

    @Query(value = """
            SELECT r.* FROM review r
            JOIN review_highlight rh ON r.id = rh.review_id
            JOIN highlight h ON rh.highlight_id = h.id
            WHERE h.normalized_value = :normalized
              AND r.is_deleted = false
              AND r.visibility = 'PUBLIC'
              AND r.id < :cursor
            ORDER BY r.id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ReviewEntity> findByHighlightNormalizedAndIdLessThan(
            @Param("normalized") String normalized,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );

    List<ReviewEntity> findByUserIdInAndCreatedAtAfter(
            List<Long> userIds,
            java.time.LocalDateTime createdAt
    );
}
