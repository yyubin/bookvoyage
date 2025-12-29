package org.yyubin.application.recommendation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.recommendation.GetReviewRecommendationsUseCase;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;
import org.yyubin.application.recommendation.port.ReviewRecommendationPort;
import org.yyubin.application.recommendation.query.GetReviewRecommendationsQuery;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.domain.review.Review;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewRecommendationApplicationService implements GetReviewRecommendationsUseCase {

    private final ReviewRecommendationPort reviewRecommendationPort;
    private final LoadReviewPort loadReviewPort;

    @Override
    public List<ReviewRecommendationResultDto> query(GetReviewRecommendationsQuery query) {
        log.info("Getting review recommendations for user {} (cursor: {}, limit: {}, forceRefresh: {})",
                query.userId(), query.cursor(), query.limit(), query.forceRefresh());

        // 1. 추천 시스템에서 reviewId 목록 가져오기
        List<ReviewRecommendationPort.RecommendationItem> recommendations =
                reviewRecommendationPort.getRecommendations(
                        query.userId(),
                        query.cursor(),
                        query.limit(),
                        query.forceRefresh()
                );

        if (recommendations.isEmpty()) {
            log.warn("No review recommendations found for user {}", query.userId());
            return List.of();
        }

        // 2. reviewId 추출
        List<Long> reviewIds = recommendations.stream()
                .map(ReviewRecommendationPort.RecommendationItem::reviewId)
                .toList();

        // 3. Review 정보 배치 조회
        Map<Long, Review> reviewMap = loadReviewsBatch(reviewIds);

        // 4. 결과 조합
        List<ReviewRecommendationResultDto> results = new ArrayList<>();
        for (ReviewRecommendationPort.RecommendationItem item : recommendations) {
            Review review = reviewMap.get(item.reviewId());
            if (review == null) {
                log.warn("Review not found: {}", item.reviewId());
                continue;
            }

            results.add(ReviewRecommendationResultDto.from(
                    review,
                    item.score(),
                    item.rank(),
                    item.source(),
                    item.reason()
            ));
        }

        log.info("Returning {} review recommendations for user {}", results.size(), query.userId());
        return results;
    }

    private Map<Long, Review> loadReviewsBatch(List<Long> reviewIds) {
        return reviewIds.stream()
                .map(reviewId -> {
                    try {
                        return loadReviewPort.loadById(reviewId);
                    } catch (Exception e) {
                        log.warn("Failed to load review {}: {}", reviewId, e.getMessage());
                        return null;
                    }
                })
                .filter(review -> review != null)
                .collect(Collectors.toMap(
                        review -> review.getId().getValue(),
                        review -> review
                ));
    }
}
