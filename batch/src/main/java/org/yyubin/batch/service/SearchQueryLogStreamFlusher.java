package org.yyubin.batch.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;
import org.yyubin.application.search.port.SearchQueryLogPort;
import org.yyubin.domain.search.SearchQuery;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchQueryLogStreamFlusher {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final StringRedisTemplate redisTemplate;
    private final SearchQueryLogPort logPort;

    @Value("${search.query-log.stream.key:search:query:log}")
    private String streamKey;

    @Value("${search.query-log.stream.group:search-query-log}")
    private String group;

    @Value("${search.query-log.stream.consumer:batch-1}")
    private String consumer;

    @Value("${search.query-log.flush.batch-size:500}")
    private int batchSize;

    @Value("${search.query-log.flush.max-batches:10}")
    private int maxBatches;

    public int flush() {
        ensureStreamGroup();
        int total = 0;

        for (int i = 0; i < maxBatches; i++) {
            List<MapRecord<String, Object, Object>> records = readPending(batchSize);
            if (records.isEmpty()) {
                records = readNew(batchSize);
            }
            if (records.isEmpty()) {
                break;
            }

            List<SearchQuery> queries = new ArrayList<>();
            for (MapRecord<String, Object, Object> record : records) {
                SearchQuery query = toSearchQuery(record.getValue());
                if (query != null) {
                    queries.add(query);
                }
            }

            try {
                if (!queries.isEmpty()) {
                    logPort.saveBatch(queries);
                    total += queries.size();
                }
                acknowledge(records.stream().map(r -> r.getId().getValue()).toList());
            } catch (Exception e) {
                log.warn("Failed to flush search query logs (records={})", records.size(), e);
                break;
            }
        }

        return total;
    }

    private List<MapRecord<String, Object, Object>> readPending(int count) {
        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();
        List<MapRecord<String, Object, Object>> records = ops.read(
            Consumer.from(group, consumer),
            StreamReadOptions.empty().count(count),
            StreamOffset.create(streamKey, ReadOffset.from("0"))
        );
        return records == null ? List.of() : records;
    }

    private List<MapRecord<String, Object, Object>> readNew(int count) {
        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();
        List<MapRecord<String, Object, Object>> records = ops.read(
            Consumer.from(group, consumer),
            StreamReadOptions.empty().count(count),
            StreamOffset.create(streamKey, ReadOffset.lastConsumed())
        );
        return records == null ? List.of() : records;
    }

    private void acknowledge(List<String> ids) {
        if (ids.isEmpty()) {
            return;
        }
        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();
        ops.acknowledge(streamKey, group, ids.toArray(new String[0]));
    }

    private void ensureStreamGroup() {
        StreamOperations<String, Object, Object> ops = redisTemplate.opsForStream();
        try {
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(streamKey))) {
                ops.add(streamKey, Map.of("init", "true"));
            }
            ops.createGroup(streamKey, ReadOffset.from("0"), group);
        } catch (Exception e) {
            log.debug("Stream group already exists or failed to create (stream={}, group={})", streamKey, group);
        }
    }

    private SearchQuery toSearchQuery(Map<Object, Object> map) {
        try {
            String queryText = parseString(map.get("queryText"));
            String normalizedQuery = parseString(map.get("normalizedQuery"));
            if (queryText == null || normalizedQuery == null) {
                return null;
            }

            Long userId = parseLong(map.get("userId"));
            String sessionId = parseString(map.get("sessionId"));
            Integer resultCount = parseInt(map.get("resultCount"));
            Long clickedContentId = parseLong(map.get("clickedContentId"));
            SearchQuery.ContentType clickedContentType = parseContentType(map.get("clickedContentType"));
            String source = parseString(map.get("source"));
            LocalDateTime createdAt = parseDateTime(map.get("createdAt"));

            return new SearchQuery(
                null,
                userId,
                sessionId,
                queryText,
                normalizedQuery,
                resultCount,
                clickedContentId,
                clickedContentType,
                source,
                createdAt != null ? createdAt : LocalDateTime.now()
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String parseString(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private Long parseLong(Object value) {
        String text = parseString(value);
        if (text == null) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(Object value) {
        String text = parseString(value);
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(Object value) {
        String text = parseString(value);
        if (text == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, TIME_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private SearchQuery.ContentType parseContentType(Object value) {
        String text = parseString(value);
        if (text == null) {
            return null;
        }
        try {
            return SearchQuery.ContentType.valueOf(text);
        } catch (Exception e) {
            return null;
        }
    }
}
