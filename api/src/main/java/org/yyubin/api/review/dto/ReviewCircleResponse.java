package org.yyubin.api.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.yyubin.domain.recommendation.CommunityTrend;
import org.yyubin.domain.recommendation.ReviewCircle;
import org.yyubin.domain.recommendation.ReviewCircleTopic;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "리뷰 서클 응답")
public record ReviewCircleResponse(
    @Schema(description = "시간 윈도우", example = "7d")
    String window,

    @Schema(description = "토픽 목록")
    List<TopicDto> topics,

    @Schema(description = "유사 사용자 수", example = "42")
    int similarUserCount,

    @Schema(description = "계산 시간")
    LocalDateTime calculatedAt,

    @Schema(description = "폴백 여부 (커뮤니티 트렌드 사용 시 true)", example = "false")
    boolean isFallback
) {
    public static ReviewCircleResponse from(ReviewCircle reviewCircle, int limit) {
        List<TopicDto> topics = reviewCircle.getTopTopics(limit).stream()
            .map(TopicDto::from)
            .toList();

        return new ReviewCircleResponse(
            reviewCircle.window(),
            topics,
            reviewCircle.similarUserCount(),
            reviewCircle.calculatedAt(),
            false
        );
    }

    public static ReviewCircleResponse fromCommunityTrend(
        CommunityTrend trend,
        String window,
        int limit
    ) {
        if (trend == null) {
            return new ReviewCircleResponse(
                window,
                List.of(),
                0,
                LocalDateTime.now(),
                true
            );
        }

        List<TopicDto> topics = trend.keywords().stream()
            .limit(limit)
            .map(keyword -> new TopicDto(keyword, 0, 0.0, trend.analyzedAt()))
            .toList();

        return new ReviewCircleResponse(
            window,
            topics,
            0,
            trend.analyzedAt(),
            true
        );
    }

    @Schema(description = "토픽 정보")
    public record TopicDto(
        @Schema(description = "키워드", example = "실존주의")
        String keyword,

        @Schema(description = "리뷰 수", example = "15")
        int reviewCount,

        @Schema(description = "점수", example = "8.5")
        double score,

        @Schema(description = "마지막 활동 시간")
        LocalDateTime lastActivityAt
    ) {
        public static TopicDto from(ReviewCircleTopic topic) {
            return new TopicDto(
                topic.keyword(),
                topic.reviewCount(),
                topic.score(),
                topic.lastActivityAt()
            );
        }
    }
}
