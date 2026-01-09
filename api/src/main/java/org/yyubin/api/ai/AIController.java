package org.yyubin.api.ai;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.ai.dto.CommunityTrendResponse;
import org.yyubin.api.ai.dto.UserAnalysisResponse;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.application.recommendation.usecase.AnalyzeCommunityTrendUseCase;
import org.yyubin.application.recommendation.usecase.AnalyzeUserPreferenceUseCase;
import org.yyubin.domain.recommendation.CommunityTrend;
import org.yyubin.domain.recommendation.UserAnalysis;

@Tag(name = "AI", description = "AI 기반 분석 API")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AnalyzeCommunityTrendUseCase analyzeCommunityTrendUseCase;
    private final AnalyzeUserPreferenceUseCase analyzeUserPreferenceUseCase;

    @Operation(
        summary = "커뮤니티 독서 트렌드 조회",
        description = "현재 커뮤니티의 전반적인 독서 분위기와 트렌드를 조회합니다. " +
                      "캐싱되어 있으며, 하루에 한 번만 LLM 분석이 수행됩니다."
    )
    @GetMapping("/community-trend")
    public ResponseEntity<CommunityTrendResponse> getCommunityTrend() {
        CommunityTrend trend = analyzeCommunityTrendUseCase.execute();
        return ResponseEntity.ok(CommunityTrendResponse.from(trend));
    }

    @Operation(
        summary = "내 독서 취향 분석",
        description = "사용자의 독서 성향을 AI로 분석하여 페르소나, 키워드, 추천 도서를 제공합니다. " +
                      "캐싱되어 있으며, 하루에 한 번만 LLM 분석이 수행됩니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/user-analysis")
    public ResponseEntity<UserAnalysisResponse> getUserAnalysis(
        @AuthenticationPrincipal Object principal
    ) {
        Long userId = PrincipalUtils.requireUserId(principal);
        UserAnalysis analysis = analyzeUserPreferenceUseCase.execute(userId);
        return ResponseEntity.ok(UserAnalysisResponse.from(analysis));
    }
}
