package org.yyubin.infrastructure.persistence.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {

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
}
