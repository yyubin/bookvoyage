package org.yyubin.infrastructure.activity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.activity.port.ActivityQueryPort;
import org.yyubin.domain.activity.ActivityItem;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.user.UserId;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkEntity;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeEntity;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserFollowingEntity;
import org.yyubin.infrastructure.persistence.user.UserFollowingJpaRepository;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityQueryAdapter implements ActivityQueryPort {

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewLikeJpaRepository reviewLikeJpaRepository;
    private final ReviewBookmarkJpaRepository reviewBookmarkJpaRepository;
    private final UserFollowingJpaRepository userFollowingJpaRepository;

    @Override
    public List<ActivityItem> loadActivities(List<Long> followingIds, Long userId, LocalDateTime cursor, int size) {
        List<ActivityItem> results = new ArrayList<>();
        int fetchSize = size;

        if (followingIds != null && !followingIds.isEmpty()) {
            results.addAll(loadReviewActivities(followingIds, cursor, fetchSize));
            results.addAll(loadLikeActivities(followingIds, cursor, fetchSize));
            results.addAll(loadBookmarkActivities(followingIds, cursor, fetchSize));
        }

        results.addAll(loadFollowActivities(userId, cursor, fetchSize));

        results.sort(Comparator
                .comparing(ActivityItem::getCreatedAt).reversed()
                .thenComparing(ActivityItem::getId, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        if (results.size() <= size) {
            return results;
        }
        return results.subList(0, size);
    }

    private List<ActivityItem> loadReviewActivities(List<Long> followingIds, LocalDateTime cursor, int size) {
        List<ReviewEntity> entities;
        if (cursor != null) {
            entities = reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityAndCreatedAtBeforeOrderByCreatedAtDesc(
                    followingIds, ReviewVisibility.PUBLIC, cursor, PageRequest.of(0, size)
            );
        } else {
            entities = reviewJpaRepository.findByUserIdInAndDeletedFalseAndVisibilityOrderByCreatedAtDesc(
                    followingIds, ReviewVisibility.PUBLIC, PageRequest.of(0, size)
            );
        }
        return entities.stream()
                .map(entity -> ActivityItem.reviewCreated(
                        entity.getId(),
                        new UserId(entity.getUserId()),
                        ReviewId.of(entity.getId()),
                        entity.getCreatedAt()
                ))
                .toList();
    }

    private List<ActivityItem> loadLikeActivities(List<Long> followingIds, LocalDateTime cursor, int size) {
        List<ReviewLikeEntity> entities;
        if (cursor != null) {
            entities = reviewLikeJpaRepository.findByUserIdInAndCreatedAtBeforeOrderByCreatedAtDesc(
                    followingIds, cursor, PageRequest.of(0, size)
            );
        } else {
            entities = reviewLikeJpaRepository.findByUserIdInOrderByCreatedAtDesc(
                    followingIds, PageRequest.of(0, size)
            );
        }
        return entities.stream()
                .map(entity -> ActivityItem.reviewLiked(
                        entity.getId(),
                        new UserId(entity.getUserId()),
                        ReviewId.of(entity.getReviewId()),
                        entity.getCreatedAt()
                ))
                .toList();
    }

    private List<ActivityItem> loadBookmarkActivities(List<Long> followingIds, LocalDateTime cursor, int size) {
        List<ReviewBookmarkEntity> entities;
        if (cursor != null) {
            entities = reviewBookmarkJpaRepository.findByUserIdInAndCreatedAtBeforeOrderByCreatedAtDesc(
                    followingIds, cursor, PageRequest.of(0, size)
            );
        } else {
            entities = reviewBookmarkJpaRepository.findByUserIdInOrderByCreatedAtDesc(
                    followingIds, PageRequest.of(0, size)
            );
        }
        return entities.stream()
                .map(entity -> ActivityItem.reviewBookmarked(
                        entity.getId(),
                        new UserId(entity.getUser().getId()),
                        ReviewId.of(entity.getReview().getId()),
                        entity.getCreatedAt()
                ))
                .toList();
    }

    private List<ActivityItem> loadFollowActivities(Long userId, LocalDateTime cursor, int size) {
        if (userId == null) {
            return List.of();
        }
        List<UserFollowingEntity> entities;
        if (cursor != null) {
            entities = userFollowingJpaRepository.findByFolloweeIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                    userId, cursor, PageRequest.of(0, size)
            );
        } else {
            entities = userFollowingJpaRepository.findByFolloweeIdOrderByCreatedAtDesc(
                    userId, PageRequest.of(0, size)
            );
        }
        return entities.stream()
                .map(entity -> ActivityItem.userFollowed(
                        entity.getId(),
                        new UserId(entity.getFollowerId()),
                        new UserId(entity.getFolloweeId()),
                        entity.getCreatedAt()
                ))
                .toList();
    }
}
