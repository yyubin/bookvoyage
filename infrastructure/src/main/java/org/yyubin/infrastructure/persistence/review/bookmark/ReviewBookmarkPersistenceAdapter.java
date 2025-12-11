package org.yyubin.infrastructure.persistence.review.bookmark;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.bookmark.port.ReviewBookmarkRepository;
import org.yyubin.domain.bookmark.ReviewBookmark;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.user.UserJpaRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewBookmarkPersistenceAdapter implements ReviewBookmarkRepository {

    private final ReviewBookmarkJpaRepository reviewBookmarkJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public boolean exists(UserId userId, ReviewId reviewId) {
        return reviewBookmarkJpaRepository.existsByUserIdAndReviewId(userId.value(), reviewId.getValue());
    }

    @Override
    @Transactional
    public ReviewBookmark save(ReviewBookmark bookmark) {
        UserEntity user = userJpaRepository.getReferenceById(bookmark.userId().value());
        ReviewEntity review = reviewJpaRepository.getReferenceById(bookmark.reviewId().getValue());
        ReviewBookmarkEntity saved = reviewBookmarkJpaRepository.save(ReviewBookmarkEntity.fromDomain(bookmark, user, review));
        return saved.toDomain();
    }

    @Override
    @Transactional
    public void delete(UserId userId, ReviewId reviewId) {
        reviewBookmarkJpaRepository.deleteByUserIdAndReviewId(userId.value(), reviewId.getValue());
    }

    @Override
    public List<ReviewBookmark> findByUserAfterCursor(UserId userId, Long cursorId, int size) {
        List<ReviewBookmarkEntity> entities;
        if (cursorId != null) {
            entities = reviewBookmarkJpaRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId.value(), cursorId);
        } else {
            entities = reviewBookmarkJpaRepository.findByUserIdOrderByIdDesc(userId.value());
        }
        return entities.stream()
                .limit(size)
                .map(ReviewBookmarkEntity::toDomain)
                .toList();
    }

    @Override
    public java.util.Optional<ReviewBookmark> findByUserAndReview(UserId userId, ReviewId reviewId) {
        return reviewBookmarkJpaRepository.findByUserIdAndReviewId(userId.value(), reviewId.getValue())
                .map(ReviewBookmarkEntity::toDomain);
    }
}
