package org.yyubin.application.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.notification.port.NotificationPublisher;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.Notification;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.user.UserId;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationEventService 테스트")
class NotificationEventServiceTest {

    @Mock
    private NotificationCreator notificationCreator;

    @Mock
    private NotificationPublisher notificationPublisher;

    @Mock
    private NotificationSettingPort notificationSettingPort;

    @InjectMocks
    private NotificationEventService notificationEventService;

    private NotificationEventPayload testPayload;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testPayload = new NotificationEventPayload(
                1L, // recipientId
                NotificationType.LIKE_ON_REVIEW,
                2L, // actorId
                100L, // reviewId
                "Someone liked your review"
        );

        testNotification = Notification.create(
                new UserId(1L),
                NotificationType.LIKE_ON_REVIEW,
                2L,
                100L,
                "Someone liked your review"
        );
    }

    @Test
    @DisplayName("알림 처리 성공 - 설정이 활성화된 경우")
    void handle_Success_WhenSettingEnabled() {
        // Given
        NotificationSetting setting = NotificationSetting.defaultFor(new UserId(1L));

        when(notificationSettingPort.load(any(UserId.class))).thenReturn(Optional.of(setting));
        when(notificationCreator.create(any(NotificationEventPayload.class))).thenReturn(testNotification);

        // When
        notificationEventService.handle(testPayload);

        // Then
        verify(notificationSettingPort).load(any(UserId.class));
        verify(notificationCreator).create(testPayload);
        verify(notificationPublisher).publish(testPayload);
    }

    @Test
    @DisplayName("알림 처리 성공 - 설정이 없는 경우 기본 설정 사용")
    void handle_Success_WhenNoSettingExists() {
        // Given
        when(notificationSettingPort.load(any(UserId.class))).thenReturn(Optional.empty());
        when(notificationCreator.create(any(NotificationEventPayload.class))).thenReturn(testNotification);

        // When
        notificationEventService.handle(testPayload);

        // Then
        verify(notificationSettingPort).load(any(UserId.class));
        verify(notificationCreator).create(testPayload);
        verify(notificationPublisher).publish(testPayload);
    }

    @Test
    @DisplayName("알림 처리 중단 - 좋아요 알림이 비활성화된 경우")
    void handle_Skip_WhenLikeNotificationDisabled() {
        // Given
        NotificationSetting setting = NotificationSetting.of(
                new UserId(1L),
                false, // likeAndComment disabled
                true,
                true
        );

        when(notificationSettingPort.load(any(UserId.class))).thenReturn(Optional.of(setting));

        // When
        notificationEventService.handle(testPayload);

        // Then
        verify(notificationSettingPort).load(any(UserId.class));
        verify(notificationCreator, never()).create(any());
        verify(notificationPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("알림 처리 중단 - 멘션 알림이 비활성화된 경우")
    void handle_Skip_WhenMentionNotificationDisabled() {
        // Given
        NotificationEventPayload mentionPayload = new NotificationEventPayload(
                1L,
                NotificationType.MENTION,
                2L,
                100L,
                "Someone mentioned you"
        );

        NotificationSetting setting = NotificationSetting.of(
                new UserId(1L),
                true,
                false, // mention disabled
                true
        );

        when(notificationSettingPort.load(any(UserId.class))).thenReturn(Optional.of(setting));

        // When
        notificationEventService.handle(mentionPayload);

        // Then
        verify(notificationSettingPort).load(any(UserId.class));
        verify(notificationCreator, never()).create(any());
        verify(notificationPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("알림 처리 중단 - 팔로이 리뷰 알림이 비활성화된 경우")
    void handle_Skip_WhenFolloweeReviewNotificationDisabled() {
        // Given
        NotificationEventPayload followeePayload = new NotificationEventPayload(
                1L,
                NotificationType.FOLLOWEE_NEW_REVIEW,
                2L,
                100L,
                "Someone you follow posted a review"
        );

        NotificationSetting setting = NotificationSetting.of(
                new UserId(1L),
                true,
                true,
                false // followee review disabled
        );

        when(notificationSettingPort.load(any(UserId.class))).thenReturn(Optional.of(setting));

        // When
        notificationEventService.handle(followeePayload);

        // Then
        verify(notificationSettingPort).load(any(UserId.class));
        verify(notificationCreator, never()).create(any());
        verify(notificationPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("댓글 알림 처리 성공")
    void handle_Success_CommentNotification() {
        // Given
        NotificationEventPayload commentPayload = new NotificationEventPayload(
                1L,
                NotificationType.COMMENT_ON_REVIEW,
                2L,
                100L,
                "Someone commented on your review"
        );

        NotificationSetting setting = NotificationSetting.defaultFor(new UserId(1L));

        when(notificationSettingPort.load(any(UserId.class))).thenReturn(Optional.of(setting));
        when(notificationCreator.create(any(NotificationEventPayload.class))).thenReturn(testNotification);

        // When
        notificationEventService.handle(commentPayload);

        // Then
        verify(notificationSettingPort).load(any(UserId.class));
        verify(notificationCreator).create(commentPayload);
        verify(notificationPublisher).publish(commentPayload);
    }
}
