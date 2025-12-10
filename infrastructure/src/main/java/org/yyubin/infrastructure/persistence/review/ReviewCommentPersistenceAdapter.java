package org.yyubin.infrastructure.persistence.review;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.port.LoadReviewCommentPort;
import org.yyubin.application.review.port.SaveReviewCommentPort;
import org.yyubin.domain.review.ReviewComment;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewCommentPersistenceAdapter implements LoadReviewCommentPort, SaveReviewCommentPort {

    private final ReviewCommentJpaRepository reviewCommentJpaRepository;

    @Override
    public ReviewComment loadById(Long commentId) {
        return reviewCommentJpaRepository.findByIdAndDeletedFalse(commentId)
                .map(ReviewCommentEntity::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
    }

    @Override
    @Transactional
    public ReviewComment save(ReviewComment comment) {
        ReviewCommentEntity entity = ReviewCommentEntity.fromDomain(comment);
        return reviewCommentJpaRepository.save(entity).toDomain();
    }

    @Override
    public List<ReviewComment> loadByReviewId(Long reviewId, Long cursor, int size) {
        List<ReviewCommentEntity> entities;
        if (cursor != null) {
            entities = reviewCommentJpaRepository.findByReviewIdAndDeletedFalseAndIdLessThanOrderByIdDesc(
                    reviewId, cursor, PageRequest.of(0, size));
        } else {
            entities = reviewCommentJpaRepository.findByReviewIdAndDeletedFalseOrderByIdDesc(
                    reviewId, PageRequest.of(0, size));
        }
        return entities.stream().map(ReviewCommentEntity::toDomain).toList();
    }
}
