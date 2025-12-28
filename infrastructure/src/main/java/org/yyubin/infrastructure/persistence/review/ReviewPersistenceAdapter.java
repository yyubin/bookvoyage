package org.yyubin.infrastructure.persistence.review;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.ReviewCountPort;
import org.yyubin.application.review.port.SaveReviewPort;
import org.yyubin.domain.review.Review;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewPersistenceAdapter implements SaveReviewPort, LoadReviewPort, ReviewCountPort {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review loadById(Long reviewId) {
        return reviewJpaRepository.findById(reviewId)
                .map(ReviewEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Review not found: " + reviewId));
    }

    @Override
    public List<Review> loadByUserId(Long userId, Long viewerId, Long cursor, int size) {
        List<ReviewEntity> entities;
        boolean isOwner = viewerId != null && viewerId.equals(userId);

        if (isOwner) {
            if (cursor != null) {
                entities = reviewJpaRepository.findByUserIdAndDeletedFalseAndIdLessThanOrderByIdDesc(userId, cursor, PageRequest.of(0, size));
            } else {
                entities = reviewJpaRepository.findByUserIdAndDeletedFalseOrderByIdDesc(userId, PageRequest.of(0, size));
            }
        } else {
            if (cursor != null) {
                entities = reviewJpaRepository.findByUserIdAndDeletedFalseAndVisibilityAndIdLessThanOrderByIdDesc(
                        userId, org.yyubin.domain.review.ReviewVisibility.PUBLIC, cursor, PageRequest.of(0, size));
            } else {
                entities = reviewJpaRepository.findByUserIdAndDeletedFalseAndVisibilityOrderByIdDesc(
                        userId, org.yyubin.domain.review.ReviewVisibility.PUBLIC, PageRequest.of(0, size));
            }
        }

        return entities.stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public List<Review> loadByHighlightNormalized(String normalizedHighlight, Long cursor, int size) {
        List<ReviewEntity> entities;
        if (cursor != null) {
            entities = reviewJpaRepository.findByHighlightNormalizedAndIdLessThan(normalizedHighlight, cursor, size);
        } else {
            entities = reviewJpaRepository.findByHighlightNormalized(normalizedHighlight, size);
        }
        return entities.stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(Long userId) {
        return reviewJpaRepository.countByUserIdAndDeletedFalse(userId);
    }

    @Override
    public List<Review> findAll() {
        return reviewJpaRepository.findAll().stream()
                .map(ReviewEntity::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Review save(Review review) {
        ReviewEntity entity = ReviewEntity.fromDomain(review);
        return reviewJpaRepository.save(entity).toDomain();
    }

    @Override
    public long countByBookId(Long bookId) {
        return reviewJpaRepository.countByBookId(bookId);
    }

    @Override
    public Double calculateAverageRating(Long bookId) {
        return reviewJpaRepository.calculateAverageRating(bookId);
    }
}
