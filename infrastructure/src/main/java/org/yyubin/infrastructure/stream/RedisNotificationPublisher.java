package org.yyubin.infrastructure.stream;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.notification.port.NotificationPublisher;

@Component
@RequiredArgsConstructor
public class RedisNotificationPublisher implements NotificationPublisher {

    private static final String STREAM_KEY = "notifications";

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void publish(NotificationEventPayload payload) {
        Map<String, String> map = new HashMap<>();
        map.put("recipientId", payload.recipientId().toString());
        map.put("type", payload.type().name());
        map.put("actorId", payload.actorId() != null ? payload.actorId().toString() : "");
        map.put("contentId", payload.contentId() != null ? payload.contentId().toString() : "");
        map.put("message", payload.message() != null ? payload.message() : "");

        redisTemplate.opsForStream().add(STREAM_KEY, map);
    }
}
