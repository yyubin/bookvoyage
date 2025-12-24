package org.yyubin.api.feed;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.api.feed.dto.FeedPageResponse;
import org.yyubin.application.feed.GetFeedUseCase;
import org.yyubin.application.feed.dto.FeedPageResult;
import org.yyubin.application.feed.query.GetFeedQuery;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final GetFeedUseCase getFeedUseCase;

    public FeedController(GetFeedUseCase getFeedUseCase) {
        this.getFeedUseCase = getFeedUseCase;
    }

    @GetMapping
    public ResponseEntity<FeedPageResponse> getFeed(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "cursor", required = false) Long cursorScore,
            @RequestParam(value = "size", required = false) @Min(1) @Max(MAX_SIZE) Integer size
    ) {
        Long userId = PrincipalUtils.requireUserId(principal);
        int pageSize = size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        FeedPageResult result = getFeedUseCase.query(new GetFeedQuery(userId, cursorScore, pageSize));
        return ResponseEntity.ok(FeedPageResponse.from(result));
    }

}
