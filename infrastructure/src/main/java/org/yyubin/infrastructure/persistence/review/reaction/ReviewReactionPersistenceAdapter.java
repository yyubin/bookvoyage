package org.yyubin.infrastructure.persistence.review.reaction;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.ReviewReactionPort;
import org.yyubin.domain.review.ReviewReaction;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewReactionPersistenceAdapter implements ReviewReactionPort {

    private final ReviewReactionJpaRepository reviewReactionJpaRepository;

    @Override
    public Optional<ReviewReaction> loadByReviewIdAndUserId(Long reviewId, Long userId) {
        return reviewReactionJpaRepository.findByReviewIdAndUserId(reviewId, userId)
                .map(ReviewReactionEntity::toDomain);
    }

    @Override
    @Transactional
    public ReviewReaction save(ReviewReaction reaction) {
        ReviewReactionEntity entity = ReviewReactionEntity.fromDomain(reaction);
        return reviewReactionJpaRepository.save(entity).toDomain();
    }

    @Override
    @Transactional
    public void delete(Long reviewId, Long userId) {
        reviewReactionJpaRepository.deleteByReviewIdAndUserId(reviewId, userId);
    }
}
