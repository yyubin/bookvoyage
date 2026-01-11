package org.yyubin.infrastructure.recommendation.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.UserActivityPort;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkEntity;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeEntity;
import org.yyubin.infrastructure.persistence.review.like.ReviewLikeJpaRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserActivityAdapter implements UserActivityPort {

    private final ReviewBookmarkJpaRepository bookmarkRepository;
    private final ReviewLikeJpaRepository likeRepository;
    private final ReviewJpaRepository reviewRepository;
    private final ReviewKeywordJpaRepository reviewKeywordRepository;

    @Override
    public UserActivity getUserActivity(Long userId) {
        // 북마크 조회
        List<ReviewBookmarkEntity> bookmarks = bookmarkRepository.findByUserIdOrderByIdDesc(userId);

        // 좋아요 조회 (북마크 제외)
        Set<Long> bookmarkedReviewIds = bookmarks.stream()
            .map(bookmark -> bookmark.getReview().getId())
            .collect(Collectors.toSet());

        List<ReviewLikeEntity> likes = likeRepository.findAll().stream()
            .filter(like -> like.getUserId().equals(userId))
            .filter(like -> !bookmarkedReviewIds.contains(like.getReviewId()))
            .toList();

        // 리뷰 ID 수집
        Set<Long> allReviewIds = new HashSet<>();
        allReviewIds.addAll(bookmarkedReviewIds);
        likes.forEach(like -> allReviewIds.add(like.getReviewId()));

        // 리뷰 정보 조회
        List<ReviewEntity> reviews = reviewRepository.findAllById(allReviewIds);
        Map<Long, ReviewEntity> reviewMap = reviews.stream()
            .collect(Collectors.toMap(ReviewEntity::getId, r -> r));

        // 키워드 조회
        List<ReviewKeywordEntity> reviewKeywords = reviewKeywordRepository.findByIdReviewIdIn(
            new ArrayList<>(allReviewIds)
        );
        Map<Long, List<String>> keywordsByReviewId = reviewKeywords.stream()
            .collect(Collectors.groupingBy(
                rk -> rk.getId().getReviewId(),
                Collectors.mapping(
                    rk -> rk.getKeyword().getNormalizedValue(),
                    Collectors.toList()
                )
            ));

        // 북마크 활동 변환
        List<ReviewActivity> bookmarkedReviews = bookmarks.stream()
            .map(bookmark -> {
                ReviewEntity review = bookmark.getReview();
                if (review == null) return null;

                return new ReviewActivity(
                    review.getId(),
                    review.getBookId(),
                    review.getGenre() != null ? review.getGenre().name() : null,
                    keywordsByReviewId.getOrDefault(review.getId(), List.of()),
                    bookmark.getCreatedAt()
                );
            })
            .filter(Objects::nonNull)
            .toList();

        // 좋아요 활동 변환
        List<ReviewActivity> likedReviews = likes.stream()
            .map(like -> {
                ReviewEntity review = reviewMap.get(like.getReviewId());
                if (review == null) return null;

                return new ReviewActivity(
                    review.getId(),
                    review.getBookId(),
                    review.getGenre() != null ? review.getGenre().name() : null,
                    keywordsByReviewId.getOrDefault(review.getId(), List.of()),
                    like.getCreatedAt()
                );
            })
            .filter(Objects::nonNull)
            .toList();

        return new UserActivity(userId, bookmarkedReviews, likedReviews);
    }

    @Override
    public List<ReviewWithKeywords> getRecentReviews(List<Long> userIds, LocalDateTime since) {
        // 리뷰 조회
        List<ReviewEntity> reviews = reviewRepository.findByUserIdInAndCreatedAtAfter(userIds, since);

        if (reviews.isEmpty()) {
            return List.of();
        }

        // 리뷰 ID 수집
        List<Long> reviewIds = reviews.stream()
            .map(ReviewEntity::getId)
            .toList();

        // 키워드 조회
        List<ReviewKeywordEntity> reviewKeywords = reviewKeywordRepository.findByIdReviewIdIn(reviewIds);
        Map<Long, List<String>> keywordsByReviewId = reviewKeywords.stream()
            .collect(Collectors.groupingBy(
                rk -> rk.getId().getReviewId(),
                Collectors.mapping(
                    rk -> rk.getKeyword().getNormalizedValue(),
                    Collectors.toList()
                )
            ));

        // 좋아요 수 조회
        var likeCounts = likeRepository.countByReviewIds(reviewIds);
        Map<Long, Long> likeCountMap = likeCounts.stream()
            .collect(Collectors.toMap(
                count -> count.getReviewId(),
                count -> count.getCount()
            ));

        // 변환
        return reviews.stream()
            .map(review -> new ReviewWithKeywords(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                review.getGenre() != null ? review.getGenre().name() : null,
                keywordsByReviewId.getOrDefault(review.getId(), List.of()),
                likeCountMap.getOrDefault(review.getId(), 0L),
                review.getCreatedAt()
            ))
            .toList();
    }
}
