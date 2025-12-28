package org.yyubin.infrastructure.persistence.review.comment;

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

    @Override
    public long countByReviewId(Long reviewId) {
        return reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(reviewId);
    }

    @Override
    public List<ReviewComment> loadRepliesByParentId(Long parentCommentId, Long cursor, int size) {
        List<ReviewCommentEntity> entities;
        if (cursor != null) {
            entities = reviewCommentJpaRepository.findByParentCommentIdAndDeletedFalseAndIdLessThanOrderByIdDesc(
                    parentCommentId, cursor, PageRequest.of(0, size));
        } else {
            entities = reviewCommentJpaRepository.findByParentCommentIdAndDeletedFalseOrderByIdDesc(
                    parentCommentId, PageRequest.of(0, size));
        }
        return entities.stream().map(ReviewCommentEntity::toDomain).toList();
    }

    @Override
    public long countRepliesByParentId(Long parentCommentId) {
        return reviewCommentJpaRepository.countByParentCommentIdAndDeletedFalse(parentCommentId);
    }

    @Override
    public java.util.Map<Long, Long> countRepliesBatch(List<Long> parentCommentIds) {
        if (parentCommentIds == null || parentCommentIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        var results = reviewCommentJpaRepository.countRepliesByParentIds(parentCommentIds);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ReviewCommentJpaRepository.ParentCommentCount::getParentId,
                        ReviewCommentJpaRepository.ParentCommentCount::getCount
                ));
    }

    @Override
    public java.util.Map<Long, Long> countByReviewIdsBatch(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        var results = reviewCommentJpaRepository.countByReviewIds(reviewIds);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        ReviewCommentJpaRepository.ReviewCommentCount::getReviewId,
                        ReviewCommentJpaRepository.ReviewCommentCount::getCount
                ));
    }
}
