package org.yyubin.infrastructure.stream;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.application.notification.service.NotificationCreator;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.user.UserId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationConsumer 테스트")
class NotificationConsumerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @Mock
    private NotificationCreator notificationCreator;

    @Mock
    private NotificationSettingPort notificationSettingPort;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    @DisplayName("스트림이 없으면 초기화 시 생성하고 그룹을 만든다")
    void init_CreatesStreamAndGroup() {
        // Given
        when(redisTemplate.hasKey("notifications")).thenReturn(false);

        // When
        notificationConsumer.init();

        // Then
        verify(streamOperations).add(eq("notifications"), eq(Map.of("init", "true")));
        verify(streamOperations).createGroup(eq("notifications"), any(), eq("notification-consumers"));
    }

    @Test
    @DisplayName("메시지가 없으면 아무 작업도 하지 않는다")
    void consume_NoMessages_NoOp() {
        // Given
        when(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .thenReturn(List.of());

        // When
        notificationConsumer.consume();

        // Then
        verify(notificationCreator, never()).create(any(NotificationEventPayload.class));
        verify(streamOperations, never()).acknowledge(anyString(), anyString(), any(RecordId.class));
    }

    @Test
    @DisplayName("잘못된 payload면 ack만 한다")
    void consume_InvalidPayload_AckOnly() {
        // Given
        var message = mock(org.springframework.data.redis.connection.stream.MapRecord.class);
        when(message.getId()).thenReturn(RecordId.of("1-0"));
        when(message.getValue()).thenReturn(Map.of("type", "INVALID"));

        when(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .thenReturn(List.of(message));

        // When
        notificationConsumer.consume();

        // Then
        verify(streamOperations).acknowledge(eq("notifications"), eq("notification-consumers"), eq(RecordId.of("1-0")));
        verify(notificationCreator, never()).create(any(NotificationEventPayload.class));
        verify(notificationSettingPort, never()).load(any(UserId.class));
    }

    @Test
    @DisplayName("설정에 의해 비활성화된 타입이면 ack만 한다")
    void consume_DisabledSetting_AckOnly() {
        // Given
        var message = mock(org.springframework.data.redis.connection.stream.MapRecord.class);
        when(message.getId()).thenReturn(RecordId.of("2-0"));
        when(message.getValue()).thenReturn(Map.of(
                "recipientId", "1",
                "type", "LIKE_ON_REVIEW",
                "actorId", "2",
                "contentId", "3",
                "message", "hello"
        ));

        NotificationSetting setting = NotificationSetting.of(new UserId(1L), false, true, true);
        when(notificationSettingPort.load(new UserId(1L))).thenReturn(Optional.of(setting));
        when(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .thenReturn(List.of(message));

        // When
        notificationConsumer.consume();

        // Then
        verify(streamOperations).acknowledge(eq("notifications"), eq("notification-consumers"), eq(RecordId.of("2-0")));
        verify(notificationCreator, never()).create(any(NotificationEventPayload.class));
    }

    @Test
    @DisplayName("설정이 없으면 알림을 생성한다")
    void consume_NoSetting_CreatesNotification() {
        // Given
        var message = mock(org.springframework.data.redis.connection.stream.MapRecord.class);
        when(message.getId()).thenReturn(RecordId.of("3-0"));
        when(message.getValue()).thenReturn(Map.of(
                "recipientId", "1",
                "type", "MENTION",
                "actorId", "2",
                "contentId", "3",
                "message", "hello"
        ));

        when(notificationSettingPort.load(new UserId(1L))).thenReturn(Optional.empty());
        when(streamOperations.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .thenReturn(List.of(message));

        // When
        notificationConsumer.consume();

        // Then
        ArgumentCaptor<NotificationEventPayload> captor =
                ArgumentCaptor.forClass(NotificationEventPayload.class);
        verify(notificationCreator).create(captor.capture());
        NotificationEventPayload payload = captor.getValue();
        assertThat(payload.recipientId()).isEqualTo(1L);
        assertThat(payload.type()).isEqualTo(NotificationType.MENTION);
        assertThat(payload.actorId()).isEqualTo(2L);
        assertThat(payload.contentId()).isEqualTo(3L);
        assertThat(payload.message()).isEqualTo("hello");
        verify(streamOperations).acknowledge(eq("notifications"), eq("notification-consumers"), eq(RecordId.of("3-0")));
    }
}
