package org.yyubin.application.recommendation.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewRecommendationResultDto(
        Long reviewId,
        Long userId,
        String authorNickname,
        Long bookId,
        String bookTitle,
        String bookCoverUrl,
        String summary,
        String content,
        Integer rating,
        LocalDateTime createdAt,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        List<ReactionInfo> topReactions,
        Double score,
        Integer rank,
        String source,
        String reason
) {
    public record ReactionInfo(String emoji, Long count) {}
}
