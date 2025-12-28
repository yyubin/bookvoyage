package org.yyubin.api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.user.dto.FollowToggleResponse;
import org.yyubin.api.user.dto.FollowPageResponse;
import org.yyubin.api.user.dto.FollowCountResponse;
import org.yyubin.api.user.dto.FollowStatusResponse;
import org.yyubin.application.user.ToggleFollowUseCase;
import org.yyubin.application.user.GetFollowingUsersUseCase;
import org.yyubin.application.user.GetFollowerUsersUseCase;
import org.yyubin.application.user.GetFollowCountUseCase;
import org.yyubin.application.user.CheckFollowStatusUseCase;
import org.yyubin.application.user.command.ToggleFollowCommand;
import org.yyubin.application.user.query.GetFollowingUsersQuery;
import org.yyubin.application.user.query.GetFollowerUsersQuery;
import org.yyubin.application.user.query.GetFollowCountQuery;
import org.yyubin.application.user.query.CheckFollowStatusQuery;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final ToggleFollowUseCase toggleFollowUseCase;
    private final GetFollowingUsersUseCase getFollowingUsersUseCase;
    private final GetFollowerUsersUseCase getFollowerUsersUseCase;
    private final GetFollowCountUseCase getFollowCountUseCase;
    private final CheckFollowStatusUseCase checkFollowStatusUseCase;

    @PostMapping("/{targetUserId}/follow")
    public ResponseEntity<FollowToggleResponse> toggleFollow(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetUserId
    ) {
        Long followerId = Long.parseLong(userDetails.getUsername());
        ToggleFollowCommand command = new ToggleFollowCommand(followerId, targetUserId);
        boolean following = toggleFollowUseCase.execute(command).following();
        return ResponseEntity.ok(new FollowToggleResponse(following));
    }

    @GetMapping("/{userId}/following")
    public ResponseEntity<FollowPageResponse> getFollowing(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                FollowPageResponse.from(
                        getFollowingUsersUseCase.getFollowing(new GetFollowingUsersQuery(userId, cursor, size))
                )
        );
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<FollowPageResponse> getFollowers(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(
                FollowPageResponse.from(
                        getFollowerUsersUseCase.getFollowers(new GetFollowerUsersQuery(userId, cursor, size))
                )
        );
    }

    @GetMapping("/{userId}/following/count")
    public ResponseEntity<FollowCountResponse> getFollowingCount(@PathVariable Long userId) {
        return ResponseEntity.ok(
                FollowCountResponse.onlyFollowing(
                        getFollowCountUseCase.getCounts(new GetFollowCountQuery(userId))
                )
        );
    }

    @GetMapping("/{userId}/followers/count")
    public ResponseEntity<FollowCountResponse> getFollowersCount(@PathVariable Long userId) {
        return ResponseEntity.ok(
                FollowCountResponse.onlyFollowers(
                        getFollowCountUseCase.getCounts(new GetFollowCountQuery(userId))
                )
        );
    }

    @GetMapping("/{targetUserId}/follow-status")
    public ResponseEntity<FollowStatusResponse> checkFollowStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetUserId
    ) {
        Long followerId = Long.parseLong(userDetails.getUsername());
        CheckFollowStatusQuery query = new CheckFollowStatusQuery(followerId, targetUserId);
        boolean following = checkFollowStatusUseCase.check(query).following();
        return ResponseEntity.ok(new FollowStatusResponse(following));
    }
}
