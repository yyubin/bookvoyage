package org.yyubin.infrastructure.search.queue;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.search.port.SearchQueryQueuePort;
import org.yyubin.domain.search.SearchQuery;

/**
 * Redis Stream adapter for enqueueing search queries
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SearchQueryQueueAdapter implements SearchQueryQueuePort {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final StringRedisTemplate redisTemplate;

    @Value("${search.query-log.stream.key:search:query:log}")
    private String streamKey;

    @Override
    public void enqueue(SearchQuery searchQuery) {
        Map<String, String> payload = toPayload(searchQuery);
        redisTemplate.opsForStream().add(streamKey, payload);
        log.trace("Enqueued search query '{}' to stream {}", searchQuery.normalizedQuery(), streamKey);
    }

    private Map<String, String> toPayload(SearchQuery searchQuery) {
        Map<String, String> payload = new HashMap<>();
        put(payload, "userId", searchQuery.userId());
        put(payload, "sessionId", searchQuery.sessionId());
        put(payload, "queryText", searchQuery.queryText());
        put(payload, "normalizedQuery", searchQuery.normalizedQuery());
        put(payload, "resultCount", searchQuery.resultCount());
        put(payload, "clickedContentId", searchQuery.clickedContentId());
        put(payload, "clickedContentType",
            searchQuery.clickedContentType() != null ? searchQuery.clickedContentType().name() : null);
        put(payload, "source", searchQuery.source());
        if (searchQuery.createdAt() != null) {
            payload.put("createdAt", searchQuery.createdAt().format(TIME_FORMAT));
        }
        return payload;
    }

    private void put(Map<String, String> payload, String key, Object value) {
        if (value == null) {
            return;
        }
        payload.put(key, value.toString());
    }
}
