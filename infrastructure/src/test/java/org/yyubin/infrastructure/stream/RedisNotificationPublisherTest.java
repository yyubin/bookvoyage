package org.yyubin.infrastructure.stream;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.domain.notification.NotificationType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisNotificationPublisher 테스트")
class RedisNotificationPublisherTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    @InjectMocks
    private RedisNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
    }

    @Test
    @DisplayName("payload를 스트림 메시지로 변환해 발행한다")
    void publish_WritesStreamEntry() {
        // Given
        NotificationEventPayload payload = new NotificationEventPayload(
                1L,
                NotificationType.MENTION,
                2L,
                3L,
                "hello"
        );

        // When
        publisher.publish(payload);

        // Then
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(streamOperations).add(eq("notifications"), captor.capture());
        Map<String, String> map = captor.getValue();
        assertThat(map.get("recipientId")).isEqualTo("1");
        assertThat(map.get("type")).isEqualTo("MENTION");
        assertThat(map.get("actorId")).isEqualTo("2");
        assertThat(map.get("contentId")).isEqualTo("3");
        assertThat(map.get("message")).isEqualTo("hello");
    }

    @Test
    @DisplayName("null 필드는 빈 문자열로 발행한다")
    void publish_NullFields_AsEmptyStrings() {
        // Given
        NotificationEventPayload payload = new NotificationEventPayload(
                1L,
                NotificationType.FOLLOWEE_NEW_REVIEW,
                null,
                null,
                null
        );

        // When
        publisher.publish(payload);

        // Then
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(streamOperations).add(eq("notifications"), captor.capture());
        Map<String, String> map = captor.getValue();
        assertThat(map.get("recipientId")).isEqualTo("1");
        assertThat(map.get("type")).isEqualTo("FOLLOWEE_NEW_REVIEW");
        assertThat(map.get("actorId")).isEqualTo("");
        assertThat(map.get("contentId")).isEqualTo("");
        assertThat(map.get("message")).isEqualTo("");
    }
}
