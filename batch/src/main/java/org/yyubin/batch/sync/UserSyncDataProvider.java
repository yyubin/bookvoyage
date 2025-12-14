package org.yyubin.batch.sync;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.batch.service.BatchUserSyncService;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionEntity;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionJpaRepository;
import org.yyubin.infrastructure.persistence.user.UserEntity;
import org.yyubin.infrastructure.persistence.wishlist.WishlistEntity;
import org.yyubin.infrastructure.persistence.wishlist.WishlistJpaRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSyncDataProvider implements BatchUserSyncService {

    private final ReviewJpaRepository reviewJpaRepository;
    private final WishlistJpaRepository wishlistJpaRepository;
    private final ReviewReactionJpaRepository reviewReactionJpaRepository;

    @Override
    public UserSyncDto buildSyncData(UserEntity user) {
        List<ReviewEntity> reviews = reviewJpaRepository.findByUserId(user.getId());
        List<UserSyncDto.ViewedBook> viewedBooks = aggregateViewedBooks(reviews);

        List<WishlistEntity> wishlists = wishlistJpaRepository.findByUserId(user.getId());
        List<UserSyncDto.WishlistedBook> wishlistedBooks = wishlists.stream()
                .map(wishlist -> new UserSyncDto.WishlistedBook(
                        wishlist.getBook().getId(),
                        wishlist.getCreatedAt()))
                .filter(item -> item.bookId() != null)
                .toList();

        List<ReviewReactionEntity> reactions = reviewReactionJpaRepository.findByUserId(user.getId());
        List<UserSyncDto.LikedReviewBook> likedReviewBooks = mapLikedReviewBooks(reactions);

        return new UserSyncDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                viewedBooks,
                wishlistedBooks,
                likedReviewBooks
        );
    }

    private List<UserSyncDto.ViewedBook> aggregateViewedBooks(List<ReviewEntity> reviews) {
        if (reviews.isEmpty()) {
            return List.of();
        }

        Map<Long, ViewedAccumulator> byBook = new HashMap<>();
        for (ReviewEntity review : reviews) {
            Long bookId = review.getBookId();
            if (bookId == null) {
                continue;
            }
            ViewedAccumulator acc = byBook.computeIfAbsent(
                    bookId,
                    ignored -> new ViewedAccumulator(review.getCreatedAt(), review.getCreatedAt(), 0)
            );

            acc.firstViewedAt = min(acc.firstViewedAt, review.getCreatedAt());
            acc.lastViewedAt = max(acc.lastViewedAt, review.getCreatedAt());
            acc.viewCount = safeAdd(acc.viewCount, review.getViewCount());
        }

        List<UserSyncDto.ViewedBook> results = new ArrayList<>();
        byBook.forEach((bookId, acc) -> results.add(
                new UserSyncDto.ViewedBook(
                        bookId,
                        acc.firstViewedAt,
                        acc.lastViewedAt,
                        acc.viewCount
                )
        ));
        return results;
    }

    private List<UserSyncDto.LikedReviewBook> mapLikedReviewBooks(List<ReviewReactionEntity> reactions) {
        if (reactions.isEmpty()) {
            return List.of();
        }

        List<Long> reviewIds = reactions.stream()
                .map(ReviewReactionEntity::getReviewId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, ReviewEntity> reviewMap = reviewIds.isEmpty()
                ? Map.of()
                : reviewJpaRepository.findAllById(reviewIds).stream()
                .collect(java.util.stream.Collectors.toMap(ReviewEntity::getId, r -> r, (a, b) -> a));

        return reactions.stream()
                .map(reaction -> {
                    ReviewEntity review = reviewMap.get(reaction.getReviewId());
                    Long bookId = review != null ? review.getBookId() : null;
                    if (bookId == null) {
                        return null;
                    }
                    LocalDateTime likedAt = reaction.getCreatedAt() != null ? reaction.getCreatedAt() : LocalDateTime.now();
                    return new UserSyncDto.LikedReviewBook(reaction.getReviewId(), bookId, likedAt);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        if (a == null) return b;
        if (b == null) return a;
        return a.isAfter(b) ? a : b;
    }

    private int safeAdd(int current, Long increment) {
        long inc = increment != null ? increment : 0L;
        long sum = (long) current + inc;
        if (sum > Integer.MAX_VALUE) {
            log.warn("View count overflow detected, capping at Integer.MAX_VALUE");
            return Integer.MAX_VALUE;
        }
        return (int) sum;
    }

    private static class ViewedAccumulator {
        private LocalDateTime firstViewedAt;
        private LocalDateTime lastViewedAt;
        private int viewCount;

        private ViewedAccumulator(LocalDateTime firstViewedAt, LocalDateTime lastViewedAt, int viewCount) {
            this.firstViewedAt = firstViewedAt;
            this.lastViewedAt = lastViewedAt;
            this.viewCount = viewCount;
        }
    }
}
