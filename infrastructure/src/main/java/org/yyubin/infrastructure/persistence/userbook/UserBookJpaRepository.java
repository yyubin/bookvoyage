package org.yyubin.infrastructure.persistence.userbook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.yyubin.domain.userbook.ReadingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBookJpaRepository extends JpaRepository<UserBookEntity, Long> {

    // Find methods
    Optional<UserBookEntity> findByUserIdAndBookIdAndDeletedFalse(Long userId, Long bookId);

    Optional<UserBookEntity> findByUserIdAndBookId(Long userId, Long bookId);

    // Existence checks
    boolean existsByUserIdAndBookIdAndDeletedFalse(Long userId, Long bookId);

    // Find by user
    List<UserBookEntity> findByUserIdAndDeletedFalse(Long userId);

    List<UserBookEntity> findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(Long userId, org.springframework.data.domain.Pageable pageable);

    List<UserBookEntity> findByUserIdAndStatusAndDeletedFalse(Long userId, ReadingStatus status);

    List<UserBookEntity> findByUserIdAndStatusAndDeletedFalseOrderByUpdatedAtDesc(Long userId, ReadingStatus status, org.springframework.data.domain.Pageable pageable);

    // Find by book
    List<UserBookEntity> findByBookIdAndDeletedFalse(Long bookId);

    // Count methods for statistics
    long countByBookIdAndStatusAndDeletedFalse(Long bookId, ReadingStatus status);

    long countByUserIdAndStatusAndDeletedFalse(Long userId, ReadingStatus status);

    long countByUserIdAndDeletedFalse(Long userId);

    @Query("""
        SELECT new org.yyubin.infrastructure.persistence.userbook.ShelfAdditionCountRow(ub.bookId, COUNT(ub.id))
        FROM UserBookEntity ub
        WHERE ub.deleted = false AND ub.createdAt >= :start AND ub.createdAt < :end
        GROUP BY ub.bookId
        ORDER BY COUNT(ub.id) DESC
        """)
    List<ShelfAdditionCountRow> findTopShelfAdditions(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            org.springframework.data.domain.Pageable pageable
    );

    // Soft delete
    @Modifying
    @Query("UPDATE UserBookEntity ub SET ub.deleted = true, ub.deletedAt = CURRENT_TIMESTAMP, ub.updatedAt = CURRENT_TIMESTAMP WHERE ub.userId = :userId AND ub.bookId = :bookId")
    void softDeleteByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
