package org.yyubin.infrastructure.stream;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.notification.NotificationCreator;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.UserId;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final String STREAM_KEY = "notifications";
    private static final String GROUP = "notification-consumers";
    private static final String CONSUMER_NAME = "consumer-1";

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationCreator notificationCreator;
    private final NotificationSettingPort notificationSettingPort;

    @Scheduled(fixedDelay = 1000)
    public void consume() {
        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();

        var messages = ops.read(
                Consumer.from(GROUP, CONSUMER_NAME),
                StreamReadOptions.empty().count(10),
                StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed())
        );

        if (messages == null || messages.isEmpty()) {
            return;
        }

        messages.forEach(message -> {
            NotificationEventPayload payload = toPayload(message.getValue());
            if (payload == null) {
                ops.acknowledge(STREAM_KEY, GROUP, message.getId());
                return;
            }

            NotificationSetting setting = notificationSettingPort.load(new UserId(payload.recipientId()))
                    .orElse(null);
            if (setting != null && !isEnabled(setting, payload.type())) {
                ops.acknowledge(STREAM_KEY, GROUP, message.getId());
                return;
            }

            notificationCreator.create(payload);
            ops.acknowledge(STREAM_KEY, GROUP, message.getId());
        });
    }

    private boolean isEnabled(NotificationSetting setting, NotificationType type) {
        return switch (type) {
            case LIKE_ON_REVIEW, COMMENT_ON_REVIEW -> setting.isLikeAndCommentEnabled();
            case MENTION -> setting.isMentionEnabled();
            case FOLLOWEE_NEW_REVIEW -> setting.isFolloweeReviewEnabled();
        };
    }

    private NotificationEventPayload toPayload(Map<Object, Object> map) {
        try {
            Long recipientId = parseLong(map.get("recipientId"));
            NotificationType type = NotificationType.valueOf((String) map.get("type"));
            Long actorId = parseLong(map.get("actorId"));
            Long contentId = parseLong(map.get("contentId"));
            String message = (String) map.get("message");
            return new NotificationEventPayload(recipientId, type, actorId, contentId, message);
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(Object val) {
        if (val == null) return null;
        String s = val.toString();
        if (s.isBlank()) return null;
        return Long.parseLong(s);
    }
}
