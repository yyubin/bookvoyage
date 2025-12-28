package org.yyubin.infrastructure.persistence.review.reaction;

import java.util.List;
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

    @Override
    public List<ReactionCount> countByReviewIdGroupByContent(Long reviewId) {
        var projections = reviewReactionJpaRepository.countByReviewIdGroupByContent(reviewId);
        return projections.stream()
                .map(p -> (ReactionCount) new ReactionCountImpl(p.getEmoji(), p.getCount()))
                .toList();
    }

    private record ReactionCountImpl(String emoji, Long count) implements ReactionCount {
        @Override
        public String getEmoji() {
            return emoji;
        }

        @Override
        public Long getCount() {
            return count;
        }
    }
}
