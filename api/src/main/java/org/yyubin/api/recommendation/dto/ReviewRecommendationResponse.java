package org.yyubin.api.recommendation.dto;

import java.util.List;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;

public record ReviewRecommendationResponse(
        List<ReviewRecommendationItemResponse> items,
        int totalItems,
        Long nextCursor,
        boolean hasMore
) {
    public static ReviewRecommendationResponse from(List<ReviewRecommendationResultDto> results, int requestedLimit) {
        List<ReviewRecommendationItemResponse> items = results.stream()
                .map(ReviewRecommendationItemResponse::from)
                .toList();

        // 다음 페이지가 있는지 확인 (요청한 limit만큼 결과가 있으면 더 있을 수 있음)
        boolean hasMore = results.size() >= requestedLimit;
        Long nextCursor = hasMore && !results.isEmpty()
                ? results.get(results.size() - 1).reviewId()
                : null;

        return new ReviewRecommendationResponse(items, items.size(), nextCursor, hasMore);
    }
}
