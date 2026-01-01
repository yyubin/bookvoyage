package org.yyubin.api.activity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.activity.dto.ActivityPageResponse;
import org.yyubin.api.common.PrincipalUtils;
import org.yyubin.application.activity.GetActivityFeedUseCase;
import org.yyubin.application.activity.dto.ActivityFeedPageResult;
import org.yyubin.application.activity.query.GetActivityFeedQuery;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final GetActivityFeedUseCase getActivityFeedUseCase;

    @GetMapping
    public ResponseEntity<ActivityPageResponse> getActivity(
            @AuthenticationPrincipal Object principal,
            @RequestParam(value = "cursor", required = false) Long cursorEpochMillis,
            @RequestParam(value = "size", required = false) @Min(1) @Max(MAX_SIZE) Integer size
    ) {
        Long userId = PrincipalUtils.requireUserId(principal);
        int pageSize = size == null ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        ActivityFeedPageResult result =
                getActivityFeedUseCase.query(new GetActivityFeedQuery(userId, cursorEpochMillis, pageSize));
        return ResponseEntity.ok(ActivityPageResponse.from(result));
    }
}
