package org.yyubin.infrastructure.persistence.review.like;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.ReviewLikePort;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewLike;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewLikePersistenceAdapter implements ReviewLikePort {

    private final ReviewLikeJpaRepository reviewLikeJpaRepository;

    @Override
    public Optional<ReviewLike> findByReviewIdAndUserId(ReviewId reviewId, UserId userId) {
        return reviewLikeJpaRepository.findByReviewIdAndUserId(reviewId.getValue(), userId.value())
                .map(ReviewLikeEntity::toDomain);
    }

    @Override
    @Transactional
    public ReviewLike save(ReviewLike reviewLike) {
        ReviewLikeEntity entity = ReviewLikeEntity.fromDomain(reviewLike);
        return reviewLikeJpaRepository.save(entity).toDomain();
    }

    @Override
    @Transactional
    public void delete(ReviewId reviewId, UserId userId) {
        reviewLikeJpaRepository.deleteByReviewIdAndUserId(reviewId.getValue(), userId.value());
    }

    @Override
    public boolean exists(ReviewId reviewId, UserId userId) {
        return reviewLikeJpaRepository.existsByReviewIdAndUserId(reviewId.getValue(), userId.value());
    }

    @Override
    public long countByReviewId(ReviewId reviewId) {
        return reviewLikeJpaRepository.countByReviewId(reviewId.getValue());
    }

    @Override
    public Map<Long, Long> countByReviewIdsBatch(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        var results = reviewLikeJpaRepository.countByReviewIds(reviewIds);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ReviewLikeJpaRepository.ReviewLikeCount::getReviewId,
                        ReviewLikeJpaRepository.ReviewLikeCount::getCount
                ));
    }
}
