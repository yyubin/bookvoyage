package org.yyubin.application.recommendation.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.port.out.UserActivityPort;
import org.yyubin.domain.recommendation.ReviewCircle;
import org.yyubin.domain.recommendation.ReviewCircleTopic;
import org.yyubin.domain.recommendation.SimilarUser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 리뷰 서클 토픽 집계 Use Case
 * 유사한 취향의 사용자들이 최근 이야기하는 주제를 집계합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AggregateReviewCircleTopicsUseCase {

    private final ReviewCircleCachePort cachePort;
    private final UserActivityPort activityPort;

    private static final int MAX_TOPICS = 20;
    private static final int MIN_REVIEW_COUNT = 2;

    /**
     * 특정 사용자의 리뷰 서클 토픽 집계
     */
    public ReviewCircle execute(Long userId, String window) {
        log.debug("Aggregating review circle topics for user {} window {}", userId, window);

        // 1. 유사 사용자 조회
        List<SimilarUser> similarUsers = cachePort.getSimilarUsers(userId);

        if (similarUsers.isEmpty()) {
            log.debug("No similar users found for user {} - returning empty circle", userId);
            return ReviewCircle.of(userId, window, List.of(), 0);
        }

        // 2. 시간 윈도우 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowStart = calculateWindowStart(window, now);

        // 3. 유사 사용자 ID 리스트
        List<Long> similarUserIds = similarUsers.stream()
            .map(SimilarUser::userId)
            .toList();

        // 4. 해당 윈도우 내의 리뷰 조회
        List<UserActivityPort.ReviewWithKeywords> reviews = activityPort.getRecentReviews(
            similarUserIds,
            windowStart
        );

        if (reviews.isEmpty()) {
            log.debug("No reviews found for similar users of user {} in window {}", userId, window);
            return ReviewCircle.of(userId, window, List.of(), similarUsers.size());
        }

        // 5. 유사도 맵 생성
        Map<Long, Double> similarityMap = similarUsers.stream()
            .collect(HashMap::new, (map, su) -> map.put(su.userId(), su.similarityScore()), HashMap::putAll);

        // 6. 토픽 집계
        Map<String, TopicAggregation> topicAggregations = new HashMap<>();

        for (UserActivityPort.ReviewWithKeywords review : reviews) {
            // 유사도 가중치
            double similarityWeight = similarityMap.getOrDefault(review.userId(), 0.5);

            // 시간 감쇠
            double timeDecay = calculateTimeDecay(review.createdAt(), now, window);

            // 참여도 (좋아요 수)
            double engagementScore = Math.log10(review.likeCount() + 1) / 3.0; // 0~1 범위로 정규화

            // 토픽 점수 계산
            double score = timeDecay * (1.0 + engagementScore) * similarityWeight;

            // 각 키워드에 대해 집계
            for (String keyword : review.keywords()) {
                topicAggregations.compute(keyword, (k, agg) -> {
                    if (agg == null) {
                        agg = new TopicAggregation(keyword);
                    }
                    agg.addReview(review.reviewId(), score, review.createdAt());
                    return agg;
                });
            }
        }

        // 7. 상위 토픽 선택
        List<ReviewCircleTopic> topics = topicAggregations.values().stream()
            .filter(agg -> agg.reviewCount >= MIN_REVIEW_COUNT)
            .map(TopicAggregation::toTopic)
            .sorted(Comparator.reverseOrder())
            .limit(MAX_TOPICS)
            .toList();

        // 8. ReviewCircle 생성
        ReviewCircle reviewCircle = ReviewCircle.of(userId, window, topics, similarUsers.size());

        // 9. Redis에 캐싱
        cachePort.saveReviewCircle(reviewCircle);

        log.debug("Aggregated {} topics for user {} from {} reviews by {} similar users",
            topics.size(), userId, reviews.size(), similarUsers.size());

        return reviewCircle;
    }

    private LocalDateTime calculateWindowStart(String window, LocalDateTime now) {
        return switch (window) {
            case "24h" -> now.minusHours(24);
            case "7d" -> now.minusDays(7);
            case "30d" -> now.minusDays(30);
            default -> now.minusDays(7); // 기본값 7일
        };
    }

    private double calculateTimeDecay(LocalDateTime activityTime, LocalDateTime now, String window) {
        long hoursDiff = Duration.between(activityTime, now).toHours();

        return switch (window) {
            case "24h" -> {
                // 24시간 윈도우: 최근 12시간은 1.0, 이후 선형 감소
                if (hoursDiff <= 12) yield 1.0;
                yield Math.max(0.0, 1.0 - (hoursDiff - 12) / 12.0);
            }
            case "7d" -> {
                // 7일 윈도우: 최근 1일은 1.0, 이후 지수 감쇠
                double daysDiff = hoursDiff / 24.0;
                if (daysDiff <= 1) yield 1.0;
                yield Math.exp(-(daysDiff - 1) / 3.0); // 3일 반감기
            }
            case "30d" -> {
                // 30일 윈도우: 최근 3일은 1.0, 이후 지수 감쇠
                double daysDiff = hoursDiff / 24.0;
                if (daysDiff <= 3) yield 1.0;
                yield Math.exp(-(daysDiff - 3) / 10.0); // 10일 반감기
            }
            default -> 1.0;
        };
    }

    /**
     * 토픽 집계 헬퍼 클래스
     */
    private static class TopicAggregation {
        private final String keyword;
        private final Set<Long> reviewIds = new HashSet<>();
        private double totalScore = 0.0;
        private LocalDateTime lastActivityAt = null;
        private int reviewCount = 0;

        TopicAggregation(String keyword) {
            this.keyword = keyword;
        }

        void addReview(Long reviewId, double score, LocalDateTime activityAt) {
            if (reviewIds.add(reviewId)) {
                reviewCount++;
            }
            totalScore += score;

            if (lastActivityAt == null || activityAt.isAfter(lastActivityAt)) {
                lastActivityAt = activityAt;
            }
        }

        ReviewCircleTopic toTopic() {
            return ReviewCircleTopic.of(
                keyword,
                reviewCount,
                totalScore,
                lastActivityAt != null ? lastActivityAt : LocalDateTime.now()
            );
        }
    }
}
