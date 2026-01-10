package org.yyubin.api.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yyubin.api.search.dto.TrendingKeywordResponse;
import org.yyubin.application.search.port.SearchKeywordTrendingPort;
import org.yyubin.application.search.service.SearchKeywordTrendingService;
import org.yyubin.domain.search.TrendingKeyword;

import java.util.List;

@Tag(name = "Search Keywords", description = "검색 키워드 추천 API")
@RestController
@RequestMapping("/api/search/keywords")
@RequiredArgsConstructor
public class SearchKeywordController {

    private final SearchKeywordTrendingService trendingService;

    @Operation(
        summary = "인기 검색 키워드 조회",
        description = "실시간 트렌딩 검색 키워드를 조회합니다. 기본적으로 24시간 기준 상위 20개 키워드를 반환합니다."
    )
    @GetMapping("/trending")
    public ResponseEntity<TrendingKeywordResponse> getTrendingKeywords(
        @Parameter(description = "조회할 키워드 개수 (기본값: 20, 최대: 50)")
        @RequestParam(value = "limit", defaultValue = "20") int limit,

        @Parameter(description = "시간 범위 (hourly: 1시간, daily: 24시간, weekly: 7일)")
        @RequestParam(value = "window", defaultValue = "daily") String window
    ) {
        // Validate limit
        if (limit < 1 || limit > 50) {
            limit = 20;
        }

        SearchKeywordTrendingPort.TimeWindow timeWindow = parseTimeWindow(window);
        List<TrendingKeyword> keywords = trendingService.getTrendingKeywordsByWindow(timeWindow, limit);

        return ResponseEntity.ok(TrendingKeywordResponse.from(keywords, timeWindow.name().toLowerCase()));
    }

    private SearchKeywordTrendingPort.TimeWindow parseTimeWindow(String window) {
        return switch (window.toLowerCase()) {
            case "hourly", "1h", "hour" -> SearchKeywordTrendingPort.TimeWindow.HOURLY;
            case "weekly", "7d", "week" -> SearchKeywordTrendingPort.TimeWindow.WEEKLY;
            default -> SearchKeywordTrendingPort.TimeWindow.DAILY;
        };
    }
}
