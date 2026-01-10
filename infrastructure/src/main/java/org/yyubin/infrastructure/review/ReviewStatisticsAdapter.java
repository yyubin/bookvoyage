package org.yyubin.infrastructure.review;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.review.port.LoadReviewCommentPort;
import org.yyubin.application.review.port.ReviewLikePort;
import org.yyubin.application.review.port.ReviewStatisticsPort;
import org.yyubin.application.review.port.ReviewViewMetricPort;

@Component
@RequiredArgsConstructor
public class ReviewStatisticsAdapter implements ReviewStatisticsPort {

    private final LoadReviewCommentPort commentPort;
    private final ReviewLikePort reviewLikePort;
    private final ReviewViewMetricPort viewMetricPort;

    @Override
    public Map<Long, ReviewStatistics> getBatchStatistics(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Map.of();
        }

        // Batch query for like counts
        Map<Long, Long> likeCounts = reviewLikePort.countByReviewIdsBatch(reviewIds);

        // Batch query for comment counts
        Map<Long, Long> commentCounts = commentPort.countByReviewIdsBatch(reviewIds);

        // Batch query for view counts
        Map<Long, Long> viewCounts = viewMetricPort.getBatchCountsWithFallback(reviewIds);

        // Combine results (순서: likeCount, commentCount, viewCount)
        Map<Long, ReviewStatistics> result = new HashMap<>();
        for (Long reviewId : reviewIds) {
            result.put(reviewId, new ReviewStatistics(
                    likeCounts.getOrDefault(reviewId, 0L).intValue(),
                    commentCounts.getOrDefault(reviewId, 0L).intValue(),
                    viewCounts.getOrDefault(reviewId, 0L)
            ));
        }

        return result;
    }
}
