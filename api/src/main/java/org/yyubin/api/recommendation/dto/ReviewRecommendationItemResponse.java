package org.yyubin.api.recommendation.dto;

import org.yyubin.api.common.TimeFormatter;
import org.yyubin.application.recommendation.dto.ReviewRecommendationResultDto;

import java.util.List;

public record ReviewRecommendationItemResponse(
        Long reviewId,
        Long userId,
        String authorNickname,
        Long bookId,
        String bookTitle,
        String bookCoverUrl,
        String summary,
        String content,
        Integer rating,
        String createdAt,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        List<ReactionInfo> topReactions,
        Double score,
        Integer rank,
        String source,
        String reason
) {
    public static ReviewRecommendationItemResponse from(ReviewRecommendationResultDto result) {
        return new ReviewRecommendationItemResponse(
                result.reviewId(),
                result.userId(),
                result.authorNickname(),
                result.bookId(),
                result.bookTitle(),
                result.bookCoverUrl(),
                result.summary(),
                result.content(),
                result.rating(),
                TimeFormatter.formatRelativeTime(result.createdAt()),
                result.likeCount(),
                result.commentCount(),
                result.viewCount(),
                result.topReactions() != null ? result.topReactions().stream()
                        .map(r -> new ReactionInfo(r.emoji(), r.count()))
                        .toList() : List.of(),
                result.score(),
                result.rank(),
                result.source(),
                result.reason()
        );
    }

    public record ReactionInfo(String emoji, Long count) {}
}
