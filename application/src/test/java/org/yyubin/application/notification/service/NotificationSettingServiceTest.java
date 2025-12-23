package org.yyubin.application.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.notification.command.UpdateNotificationSettingCommand;
import org.yyubin.application.notification.dto.NotificationSettingResult;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.UserId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSettingService 테스트")
class NotificationSettingServiceTest {

    @Mock
    private NotificationSettingPort notificationSettingPort;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    private UserId userId;
    private NotificationSetting defaultSetting;

    @BeforeEach
    void setUp() {
        userId = new UserId(1L);
        defaultSetting = NotificationSetting.defaultFor(userId);
    }

    @Test
    @DisplayName("알림 설정 조회 성공 - 기존 설정이 있는 경우")
    void get_SuccessWithExistingSetting() {
        // Given
        NotificationSetting setting = NotificationSetting.of(userId, true, false, true);
        when(notificationSettingPort.load(userId)).thenReturn(Optional.of(setting));

        // When
        NotificationSettingResult result = notificationSettingService.get(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.likeAndCommentEnabled()).isTrue();
        assertThat(result.mentionEnabled()).isFalse();
        assertThat(result.followeeReviewEnabled()).isTrue();

        verify(notificationSettingPort).load(userId);
        verify(notificationSettingPort, never()).save(any());
    }

    @Test
    @DisplayName("알림 설정 조회 성공 - 설정이 없는 경우 기본값 생성")
    void get_SuccessWithDefaultSetting() {
        // Given
        when(notificationSettingPort.load(userId)).thenReturn(Optional.empty());
        when(notificationSettingPort.save(any(NotificationSetting.class))).thenReturn(defaultSetting);

        // When
        NotificationSettingResult result = notificationSettingService.get(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.likeAndCommentEnabled()).isTrue();
        assertThat(result.mentionEnabled()).isTrue();
        assertThat(result.followeeReviewEnabled()).isTrue();

        verify(notificationSettingPort).load(userId);
        verify(notificationSettingPort).save(any(NotificationSetting.class));
    }

    @Test
    @DisplayName("알림 설정 업데이트 성공 - 모든 설정 변경")
    void update_SuccessWithAllChanges() {
        // Given
        UpdateNotificationSettingCommand command = new UpdateNotificationSettingCommand(
                1L,
                false,
                false,
                false
        );

        NotificationSetting existingSetting = NotificationSetting.defaultFor(userId);
        when(notificationSettingPort.load(userId)).thenReturn(Optional.of(existingSetting));

        NotificationSetting updatedSetting = NotificationSetting.of(userId, false, false, false);
        when(notificationSettingPort.save(any(NotificationSetting.class))).thenReturn(updatedSetting);

        // When
        NotificationSettingResult result = notificationSettingService.update(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.likeAndCommentEnabled()).isFalse();
        assertThat(result.mentionEnabled()).isFalse();
        assertThat(result.followeeReviewEnabled()).isFalse();

        verify(notificationSettingPort).load(userId);
        verify(notificationSettingPort).save(any(NotificationSetting.class));
    }

    @Test
    @DisplayName("알림 설정 업데이트 성공 - 일부 설정만 변경")
    void update_SuccessWithPartialChanges() {
        // Given
        UpdateNotificationSettingCommand command = new UpdateNotificationSettingCommand(
                1L,
                false,
                null,
                null
        );

        NotificationSetting existingSetting = NotificationSetting.defaultFor(userId);
        when(notificationSettingPort.load(userId)).thenReturn(Optional.of(existingSetting));
        when(notificationSettingPort.save(any(NotificationSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationSettingResult result = notificationSettingService.update(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.likeAndCommentEnabled()).isFalse();
        assertThat(result.mentionEnabled()).isTrue();
        assertThat(result.followeeReviewEnabled()).isTrue();

        verify(notificationSettingPort).load(userId);
        verify(notificationSettingPort).save(any(NotificationSetting.class));
    }

    @Test
    @DisplayName("알림 설정 업데이트 성공 - 설정이 없는 경우 기본값으로 시작")
    void update_SuccessWithoutExistingSetting() {
        // Given
        UpdateNotificationSettingCommand command = new UpdateNotificationSettingCommand(
                1L,
                false,
                true,
                false
        );

        when(notificationSettingPort.load(userId)).thenReturn(Optional.empty());
        when(notificationSettingPort.save(any(NotificationSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationSettingResult result = notificationSettingService.update(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.likeAndCommentEnabled()).isFalse();
        assertThat(result.mentionEnabled()).isTrue();
        assertThat(result.followeeReviewEnabled()).isFalse();

        verify(notificationSettingPort).load(userId);
        verify(notificationSettingPort).save(any(NotificationSetting.class));
    }

    @Test
    @DisplayName("알림 설정 업데이트 - enable과 disable 토글 테스트")
    void update_ToggleSettings() {
        // Given
        UpdateNotificationSettingCommand enableCommand = new UpdateNotificationSettingCommand(
                1L,
                true,
                true,
                true
        );

        NotificationSetting setting = NotificationSetting.of(userId, false, false, false);
        when(notificationSettingPort.load(userId)).thenReturn(Optional.of(setting));
        when(notificationSettingPort.save(any(NotificationSetting.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        NotificationSettingResult result = notificationSettingService.update(enableCommand);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.likeAndCommentEnabled()).isTrue();
        assertThat(result.mentionEnabled()).isTrue();
        assertThat(result.followeeReviewEnabled()).isTrue();

        verify(notificationSettingPort).load(userId);
        verify(notificationSettingPort).save(any(NotificationSetting.class));
    }
}
