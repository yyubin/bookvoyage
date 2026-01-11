package org.yyubin.api.review;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.review.dto.ReviewCircleResponse;
import org.yyubin.application.recommendation.port.out.ReviewCircleCachePort;
import org.yyubin.application.recommendation.usecase.AggregateReviewCircleTopicsUseCase;
import org.yyubin.application.recommendation.usecase.AnalyzeCommunityTrendUseCase;
import org.yyubin.domain.recommendation.CommunityTrend;
import org.yyubin.domain.recommendation.ReviewCircle;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;

@Tag(name = "Review Circle", description = "리뷰 서클 API - 비슷한 취향의 독자들이 지금 이야기하는 주제를 제공합니다.")
@RestController
@RequestMapping("/api/review-circle")
@RequiredArgsConstructor
public class ReviewCircleController {

    private final ReviewCircleCachePort cachePort;
    private final AggregateReviewCircleTopicsUseCase aggregateTopicsUseCase;
    private final AnalyzeCommunityTrendUseCase analyzeCommunityTrendUseCase;

    @Operation(
        summary = "리뷰 서클 토픽 조회",
        description = """
            비슷한 취향의 독자들이 지금 이야기하는 주제를 조회합니다.

            **로그인 시**: 사용자 취향 기반 개인화된 토픽 반환
            **비로그인 시**: 전역 커뮤니티 트렌드 반환

            캐시된 데이터를 우선 반환하며, 없는 경우 실시간으로 계산합니다.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공 (로그인/비로그인 모두 가능)")
    })
    @GetMapping("/topics")
    public ResponseEntity<ReviewCircleResponse> getReviewCircleTopics(
        @Parameter(description = "시간 윈도우 (24h, 7d, 30d)", example = "7d")
        @RequestParam(value = "window", defaultValue = "7d") String window,
        @Parameter(description = "조회할 토픽 개수", example = "10")
        @RequestParam(value = "limit", defaultValue = "10") int limit,
        @Parameter(hidden = true) @AuthenticationPrincipal Object principal
    ) {
        Long userId = resolveUserId(principal);

        // 비로그인 사용자: 전역 커뮤니티 트렌드 반환
        if (userId == null) {
            CommunityTrend communityTrend = analyzeCommunityTrendUseCase
                .findCachedTrend()
                .orElseGet(() -> analyzeCommunityTrendUseCase.execute());

            return ResponseEntity.ok(ReviewCircleResponse.fromCommunityTrend(
                communityTrend,
                window,
                limit
            ));
        }

        // 로그인 사용자: 개인화된 리뷰 서클 반환
        // 1. 캐시 조회
        ReviewCircle reviewCircle = cachePort.getReviewCircle(userId, window)
            .orElseGet(() -> {
                // 2. 캐시 미스 시 실시간 계산
                return aggregateTopicsUseCase.execute(userId, window);
            });

        // 3. 폴백: 유사 사용자가 없으면 커뮤니티 트렌드 반환
        if (reviewCircle.topics().isEmpty()) {
            CommunityTrend communityTrend = analyzeCommunityTrendUseCase
                .findCachedTrend()
                .orElse(null);

            return ResponseEntity.ok(ReviewCircleResponse.fromCommunityTrend(
                communityTrend,
                window,
                limit
            ));
        }

        return ResponseEntity.ok(ReviewCircleResponse.from(reviewCircle, limit));
    }

    /**
     * Principal에서 userId를 안전하게 추출
     * 로그인하지 않은 경우 null 반환 (예외 발생 안 함)
     */
    private Long resolveUserId(Object principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUserId();
        }
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            try {
                return Long.parseLong(userDetails.getUsername());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
