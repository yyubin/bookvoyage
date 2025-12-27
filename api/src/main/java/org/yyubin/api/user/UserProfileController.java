package org.yyubin.api.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.user.dto.ProfileImageUploadRequest;
import org.yyubin.api.user.dto.ProfileImageUploadResponse;
import org.yyubin.api.user.dto.UpdateUserBioRequest;
import org.yyubin.api.user.dto.UpdateUserNicknameRequest;
import org.yyubin.api.user.dto.UpdateUserProfileImageRequest;
import org.yyubin.api.user.dto.UpdateUserTasteTagRequest;
import org.yyubin.api.user.dto.UserProfileResponse;
import org.yyubin.application.user.command.UpdateUserBioCommand;
import org.yyubin.application.user.query.UpdateUserCommandHandler;
import org.yyubin.application.user.command.UpdateUserNicknameCommand;
import org.yyubin.application.user.command.UpdateUserProfileImageUrlCommand;
import org.yyubin.application.user.command.UpdateUserTasteTagCommand;
import org.yyubin.application.user.dto.ProfileImageUploadUrlResult;
import org.yyubin.application.user.query.GenerateProfileImageUploadUrlQuery;
import org.yyubin.application.user.query.GenerateProfileImageUploadUrlQueryHandler;
import org.yyubin.application.user.query.GetUserProfileQuery;
import org.yyubin.application.user.query.GetUserProfileQueryHandler;
import org.yyubin.application.user.query.UserProfileResult;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UpdateUserCommandHandler updateUserCommandHandler;
    private final GetUserProfileQueryHandler getUserProfileQueryHandler;
    private final GenerateProfileImageUploadUrlQueryHandler generateProfileImageUploadUrlQueryHandler;

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

    @PatchMapping("/me/taste-tag")
    public ResponseEntity<Void> updateMyTasteTag(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateUserTasteTagRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UpdateUserTasteTagCommand command = new UpdateUserTasteTagCommand(userId, request.tasteTag());
        updateUserCommandHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/profile-image/upload-url")
    public ResponseEntity<ProfileImageUploadResponse> getProfileImageUploadUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileImageUploadRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        GenerateProfileImageUploadUrlQuery query = new GenerateProfileImageUploadUrlQuery(userId, request.filename());
        ProfileImageUploadUrlResult result = generateProfileImageUploadUrlQueryHandler.handle(query);
        return ResponseEntity.ok(
                ProfileImageUploadResponse.of(
                        result.presignedUrl(),
                        result.fileUrl(),
                        result.objectKey()
                )
        );
    }
}
