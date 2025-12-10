package org.yyubin.api.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.user.dto.NotificationSettingResponse;
import org.yyubin.api.user.dto.UpdateNotificationSettingRequest;
import org.yyubin.application.notification.GetNotificationSettingUseCase;
import org.yyubin.application.notification.UpdateNotificationSettingUseCase;
import org.yyubin.application.notification.command.UpdateNotificationSettingCommand;
import org.yyubin.domain.user.UserId;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class NotificationSettingController {

    private final GetNotificationSettingUseCase getNotificationSettingUseCase;
    private final UpdateNotificationSettingUseCase updateNotificationSettingUseCase;

    @GetMapping("/me/notification-settings")
    public ResponseEntity<NotificationSettingResponse> getMyNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(
                NotificationSettingResponse.from(getNotificationSettingUseCase.get(new UserId(userId)))
        );
    }

    @PatchMapping("/me/notification-settings")
    public ResponseEntity<NotificationSettingResponse> updateMyNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateNotificationSettingRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        var result = updateNotificationSettingUseCase.update(
                new UpdateNotificationSettingCommand(
                        userId,
                        request.likeComment(),
                        request.mention(),
                        request.followeeReview()
                )
        );
        return ResponseEntity.ok(NotificationSettingResponse.from(result));
    }
}
