package org.yyubin.infrastructure.stream.kafka;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.yyubin.application.event.EventPayload;
import org.yyubin.infrastructure.config.SessionBoostProperties;

@Component
@Slf4j
@RequiredArgsConstructor
public class SessionBoostEventConsumer {

    private final RedisTemplate<String, String> redisTemplate;
    private final SessionBoostProperties properties;
    private final DefaultRedisScript<Long> trimScript = new DefaultRedisScript<>(TRIM_SCRIPT, Long.class);

    private static final String BUCKET_BOOKS = "books";
    private static final String TRIM_SCRIPT = """
            local key = KEYS[1]
            local max = tonumber(ARGV[1])
            local entries = redis.call('HGETALL', key)
            if entries == nil or #entries == 0 then
              return 0
            end
            local pairs = {}
            for i=1,#entries,2 do
              local field = entries[i]
              local value = tonumber(entries[i+1])
              if value then
                table.insert(pairs, {field, value})
              end
            end
            table.sort(pairs, function(a,b) return a[2] > b[2] end)
            if #pairs <= max then
              return #pairs
            end
            for i=max+1,#pairs do
              redis.call('HDEL', key, pairs[i][1])
            end
            return max
            """;

    @KafkaListener(
            topics = {"events.review", "events.reaction", "events.wishlist-bookmark", "events.feed"},
            groupId = "cg-session-boost"
    )
    public void consume(EventPayload payload) {
        if (payload.userId() == null) {
            return;
        }

        List<BoostOp> ops = mapToOps(payload);
        if (ops.isEmpty()) {
            return;
        }

        try {
            redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
                for (BoostOp op : ops) {
                    byte[] key = redisTemplate.getStringSerializer().serialize(op.key());
                    byte[] field = redisTemplate.getStringSerializer().serialize(op.field());
                    connection.hashCommands().hIncrBy(key, field, op.delta());
                    connection.keyCommands().expire(key, properties.getTtlSeconds());
                    connection.scriptingCommands().eval(
                            trimScript.getScriptAsString().getBytes(),
                            ReturnType.INTEGER,
                            1,
                            key,
                            RedisStringCommands.toBytes(properties.getMaxEntries())
                    );
                }
                return null;
            });
        } catch (DataAccessException ex) {
            log.warn("Failed to apply session boost for user={} eventType={} error={}", payload.userId(), payload.eventType(), ex.toString());
        }
    }

    private List<BoostOp> mapToOps(EventPayload payload) {
        List<BoostOp> ops = new ArrayList<>();
        String eventType = payload.eventType();
        if (eventType == null) {
            return ops;
        }

        switch (eventType) {
            case "REVIEW_CREATED" -> {
                Long bookId = asLong(payload.metadata(), "bookId");
                if (bookId != null) {
                    ops.add(boostBooks(payload.userId(), bookId, 0.05));
                }
            }
            case "WISHLIST_ADD" -> {
                Long bookId = asLong(payload.metadata(), "bookId");
                if (bookId != null) {
                    ops.add(boostBooks(payload.userId(), bookId, 0.2));
                }
            }
            case "BOOKMARK_ADD" -> {
                Long reviewId = asLong(payload.metadata(), "reviewId");
                if (reviewId != null) {
                    // Bookmark는 review 기준이라 book 정보가 없어도 리뷰 선호를 약하게 반영
                    ops.add(boostBooks(payload.userId(), reviewId, 0.05));
                }
            }
            default -> {
                // no-op
            }
        }
        return ops;
    }

    private BoostOp boostBooks(Long userId, Long id, double delta) {
        return new BoostOp(key(userId, BUCKET_BOOKS), id.toString(), delta);
    }

    private String key(Long userId, String bucket) {
        return "session:user:%s:%s".formatted(userId, bucket);
    }

    private Long asLong(Map<String, Object> metadata, String key) {
        if (metadata == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private record BoostOp(String key, String field, double delta) {
    }
}
