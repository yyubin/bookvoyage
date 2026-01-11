package org.yyubin.application.recommendation.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.port.out.UserActivityPort;
import org.yyubin.domain.recommendation.UserTasteVector;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 사용자 취향 벡터 생성 Use Case
 * 사용자의 활동 기록을 기반으로 키워드/장르 가중치를 계산합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuildUserTasteVectorUseCase {

    private final UserActivityPort activityPort;
    private final ReviewCircleCachePort cachePort;

    private static final double BOOKMARK_WEIGHT = 1.0;
    private static final double LIKE_WEIGHT = 0.6;
    private static final int MAX_DAYS_LOOKBACK = 180; // 6개월

    public UserTasteVector execute(Long userId) {
        log.info("Building taste vector for user {}", userId);

        // 1. 사용자 활동 조회
        UserActivityPort.UserActivity activity = activityPort.getUserActivity(userId);

        if (activity.bookmarkedReviews().isEmpty() && activity.likedReviews().isEmpty()) {
            log.debug("User {} has no bookmarks or likes - returning empty taste vector", userId);
            return UserTasteVector.of(userId, Map.of());
        }

        // 2. 취향 벡터 계산
        Map<String, Double> tasteVector = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 북마크 기반 벡터 계산
        for (UserActivityPort.ReviewActivity review : activity.bookmarkedReviews()) {
            double timeDecay = calculateTimeDecay(review.activityAt(), now);
            double weight = BOOKMARK_WEIGHT * timeDecay;

            // 장르 추가
            if (review.genre() != null && !review.genre().isBlank()) {
                String genreKey = "genre:" + review.genre();
                tasteVector.merge(genreKey, weight, Double::sum);
            }

            // 키워드 추가
            for (String keyword : review.keywords()) {
                String keywordKey = "keyword:" + keyword;
                tasteVector.merge(keywordKey, weight, Double::sum);
            }
        }

        // 좋아요 기반 벡터 계산
        for (UserActivityPort.ReviewActivity review : activity.likedReviews()) {
            double timeDecay = calculateTimeDecay(review.activityAt(), now);
            double weight = LIKE_WEIGHT * timeDecay;

            // 장르 추가
            if (review.genre() != null && !review.genre().isBlank()) {
                String genreKey = "genre:" + review.genre();
                tasteVector.merge(genreKey, weight, Double::sum);
            }

            // 키워드 추가
            for (String keyword : review.keywords()) {
                String keywordKey = "keyword:" + keyword;
                tasteVector.merge(keywordKey, weight, Double::sum);
            }
        }

        // 3. 정규화 (L2 norm)
        Map<String, Double> normalizedVector = normalizeVector(tasteVector);

        // 4. UserTasteVector 생성
        UserTasteVector userTasteVector = UserTasteVector.of(userId, normalizedVector);

        // 5. Redis에 캐싱
        cachePort.saveTasteVector(userTasteVector);

        log.info("Built taste vector for user {} with {} features", userId, normalizedVector.size());
        return userTasteVector;
    }

    /**
     * 시간 감쇠 계산
     * 최근 활동일수록 높은 가중치
     */
    private double calculateTimeDecay(LocalDateTime activityTime, LocalDateTime now) {
        long daysDiff = Duration.between(activityTime, now).toDays();

        if (daysDiff > MAX_DAYS_LOOKBACK) {
            return 0.0;
        }

        // 지수 감쇠: e^(-daysDiff/90)
        // 90일 전 활동은 약 1/e (37%) 가중치
        return Math.exp(-daysDiff / 90.0);
    }

    /**
     * 벡터 정규화 (L2 norm)
     */
    private Map<String, Double> normalizeVector(Map<String, Double> vector) {
        if (vector.isEmpty()) {
            return Map.of();
        }

        double sumSquares = vector.values().stream()
            .mapToDouble(v -> v * v)
            .sum();

        double norm = Math.sqrt(sumSquares);

        if (norm == 0.0) {
            return vector;
        }

        Map<String, Double> normalized = new HashMap<>();
        for (Map.Entry<String, Double> entry : vector.entrySet()) {
            normalized.put(entry.getKey(), entry.getValue() / norm);
        }

        return normalized;
    }
}
