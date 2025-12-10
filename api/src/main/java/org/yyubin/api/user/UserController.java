package org.yyubin.api.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.yyubin.api.user.dto.UpdateUserBioRequest;
import org.yyubin.api.user.dto.UpdateUserNicknameRequest;
import org.yyubin.api.user.dto.UpdateUserProfileImageRequest;
import org.yyubin.api.user.dto.UserProfileResponse;
import org.yyubin.api.user.dto.FollowToggleResponse;
import org.yyubin.api.user.dto.FollowPageResponse;
import org.yyubin.api.user.dto.FollowCountResponse;
import org.yyubin.application.user.ToggleFollowUseCase;
import org.yyubin.application.user.GetFollowingUsersUseCase;
import org.yyubin.application.user.GetFollowerUsersUseCase;
import org.yyubin.application.user.GetFollowCountUseCase;
import org.yyubin.application.user.command.UpdateUserBioCommand;
import org.yyubin.application.user.command.UpdateUserCommandHandler;
import org.yyubin.application.user.command.UpdateUserNicknameCommand;
import org.yyubin.application.user.command.UpdateUserProfileImageUrlCommand;
import org.yyubin.application.user.query.GetUserProfileQuery;
import org.yyubin.application.user.query.GetUserProfileQueryHandler;
import org.yyubin.application.user.query.UserProfileResult;
import org.yyubin.application.user.command.ToggleFollowCommand;
import org.yyubin.application.user.query.GetFollowingUsersQuery;
import org.yyubin.application.user.query.GetFollowerUsersQuery;
import org.yyubin.application.user.query.GetFollowCountQuery;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserCommandHandler updateUserCommandHandler;
    private final GetUserProfileQueryHandler getUserProfileQueryHandler;
    private final ToggleFollowUseCase toggleFollowUseCase;
    private final GetFollowingUsersUseCase getFollowingUsersUseCase;
    private final GetFollowerUsersUseCase getFollowerUsersUseCase;
    private final GetFollowCountUseCase getFollowCountUseCase;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable Long userId) {
        GetUserProfileQuery query = new GetUserProfileQuery(userId);
        UserProfileResult result = getUserProfileQueryHandler.handle(query);
        return ResponseEntity.ok(UserProfileResponse.from(result));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        GetUserProfileQuery query = new GetUserProfileQuery(userId);
        UserProfileResult result = getUserProfileQueryHandler.handle(query);
        return ResponseEntity.ok(UserProfileResponse.from(result));
    }

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

    @PatchMapping("/me/bio")
    public ResponseEntity<Void> updateMyBio(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserBioRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UpdateUserBioCommand command = new UpdateUserBioCommand(userId, request.bio());
        updateUserCommandHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<Void> updateMyNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserNicknameRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UpdateUserNicknameCommand command = new UpdateUserNicknameCommand(userId, request.nickname());
        updateUserCommandHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/profile-image")
    public ResponseEntity<Void> updateMyProfileImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserProfileImageRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UpdateUserProfileImageUrlCommand command = new UpdateUserProfileImageUrlCommand(userId, request.imageUrl());
        updateUserCommandHandler.handle(command);
        return ResponseEntity.noContent().build();
    }


}
