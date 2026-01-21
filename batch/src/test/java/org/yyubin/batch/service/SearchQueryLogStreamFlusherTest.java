package org.yyubin.batch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.search.port.SearchQueryLogPort;
import org.yyubin.domain.search.SearchQuery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchQueryLogStreamFlusher 테스트")
class SearchQueryLogStreamFlusherTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SearchQueryLogPort logPort;

    @Mock
    private StreamOperations<String, Object, Object> streamOperations;

    private SearchQueryLogStreamFlusher flusher;

    @BeforeEach
    void setUp() {
        flusher = new SearchQueryLogStreamFlusher(redisTemplate, logPort);
        ReflectionTestUtils.setField(flusher, "streamKey", "search:query:log");
        ReflectionTestUtils.setField(flusher, "group", "search-query-log");
        ReflectionTestUtils.setField(flusher, "consumer", "batch-1");
        ReflectionTestUtils.setField(flusher, "batchSize", 500);
        ReflectionTestUtils.setField(flusher, "maxBatches", 10);
    }

    @SuppressWarnings("unchecked")
    private void setupBasicMocks() {
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(redisTemplate.hasKey("search:query:log")).thenReturn(true);
    }

    @SuppressWarnings("unchecked")
    private MapRecord<String, Object, Object> createMockRecord(String recordIdValue, Map<Object, Object> data) {
        RecordId recordId = RecordId.of(recordIdValue);
        MapRecord<String, Object, Object> record = mock(MapRecord.class);
        when(record.getValue()).thenReturn(data);
        when(record.getId()).thenReturn(recordId);
        return record;
    }

    @Test
    @DisplayName("플러시할 레코드가 없는 경우")
    void flush_NoRecords() {
        // Given
        setupBasicMocks();
        doReturn(List.of()).when(streamOperations).read(
                any(Consumer.class),
                any(StreamReadOptions.class),
                any(StreamOffset.class)
        );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(0);
        verify(logPort, never()).saveBatch(any());
    }

    @Test
    @DisplayName("레코드 플러시 성공")
    void flush_Success() {
        // Given
        setupBasicMocks();

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        recordData.put("normalizedQuery", "test query");
        recordData.put("userId", "1");
        recordData.put("sessionId", "session-123");
        recordData.put("resultCount", "10");
        recordData.put("source", "WEB");
        recordData.put("createdAt", LocalDateTime.now().toString());

        MapRecord<String, Object, Object> record = createMockRecord("1234567890-0", recordData);

        doReturn(List.of(record))
                .doReturn(List.of())
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(1);
        verify(logPort).saveBatch(any());
    }

    @Test
    @DisplayName("필수 필드 누락 시 레코드 무시")
    void flush_IgnoresMissingRequiredFields() {
        // Given
        setupBasicMocks();

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        // normalizedQuery 누락

        MapRecord<String, Object, Object> record = createMockRecord("1234567890-0", recordData);

        doReturn(List.of(record))
                .doReturn(List.of())
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(0);
        verify(logPort, never()).saveBatch(any());
    }

    @Test
    @DisplayName("저장 실패 시 중단")
    void flush_StopsOnSaveFailure() {
        // Given
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(redisTemplate.hasKey("search:query:log")).thenReturn(true);

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        recordData.put("normalizedQuery", "test query");

        // Use lenient mock for record since getId() may not be called if save fails first
        RecordId recordId = RecordId.of("1234567890-0");
        @SuppressWarnings("unchecked")
        MapRecord<String, Object, Object> record = mock(MapRecord.class);
        lenient().when(record.getValue()).thenReturn(recordData);
        lenient().when(record.getId()).thenReturn(recordId);

        // readPending returns record on first call, then loop breaks on save failure
        doReturn(List.of(record))
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );
        doThrow(new RuntimeException("Save failed")).when(logPort).saveBatch(any());

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(0);
        verify(logPort).saveBatch(any());
    }

    @Test
    @DisplayName("Stream 그룹 생성 시도")
    void flush_EnsuresStreamGroup() {
        // Given
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(redisTemplate.hasKey("search:query:log")).thenReturn(false);
        doReturn(List.of()).when(streamOperations).read(
                any(Consumer.class),
                any(StreamReadOptions.class),
                any(StreamOffset.class)
        );

        // When
        flusher.flush();

        // Then
        verify(streamOperations).add(eq("search:query:log"), any(Map.class));
        verify(streamOperations).createGroup(eq("search:query:log"), any(ReadOffset.class), eq("search-query-log"));
    }

    @Test
    @DisplayName("Stream 그룹이 이미 존재할 때 예외 무시")
    void flush_IgnoresExistingGroupException() {
        // Given
        when(redisTemplate.opsForStream()).thenReturn(streamOperations);
        when(redisTemplate.hasKey("search:query:log")).thenReturn(true);
        doThrow(new RuntimeException("BUSYGROUP Consumer Group name already exists"))
                .when(streamOperations).createGroup(any(String.class), any(ReadOffset.class), any(String.class));
        doReturn(List.of()).when(streamOperations).read(
                any(Consumer.class),
                any(StreamReadOptions.class),
                any(StreamOffset.class)
        );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    @DisplayName("여러 배치 처리")
    void flush_ProcessesMultipleBatches() {
        // Given
        setupBasicMocks();

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        recordData.put("normalizedQuery", "test query");

        MapRecord<String, Object, Object> record1 = createMockRecord("1234567890-0", recordData);
        MapRecord<String, Object, Object> record2 = createMockRecord("1234567890-1", recordData);

        doReturn(List.of(record1))
                .doReturn(List.of(record2))
                .doReturn(List.of())
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(2);
        verify(logPort, times(2)).saveBatch(any());
    }

    @Test
    @DisplayName("Acknowledge 호출 확인")
    void flush_AcknowledgesRecords() {
        // Given
        setupBasicMocks();

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        recordData.put("normalizedQuery", "test query");

        MapRecord<String, Object, Object> record = createMockRecord("1234567890-0", recordData);

        doReturn(List.of(record))
                .doReturn(List.of())
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );

        // When
        flusher.flush();

        // Then
        verify(streamOperations).acknowledge(eq("search:query:log"), eq("search-query-log"), any(String[].class));
    }

    @Test
    @DisplayName("잘못된 숫자 형식 무시")
    void flush_IgnoresInvalidNumberFormat() {
        // Given
        setupBasicMocks();

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        recordData.put("normalizedQuery", "test query");
        recordData.put("userId", "not_a_number");
        recordData.put("resultCount", "invalid");

        MapRecord<String, Object, Object> record = createMockRecord("1234567890-0", recordData);

        doReturn(List.of(record))
                .doReturn(List.of())
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SearchQuery>> captor = ArgumentCaptor.forClass(List.class);
        verify(logPort).saveBatch(captor.capture());
        SearchQuery saved = captor.getValue().get(0);
        assertThat(saved.userId()).isNull();
        assertThat(saved.resultCount()).isNull();
    }

    @Test
    @DisplayName("빈 문자열 필드 null 처리")
    void flush_TreatsBlankAsNull() {
        // Given
        setupBasicMocks();

        Map<Object, Object> recordData = new HashMap<>();
        recordData.put("queryText", "test query");
        recordData.put("normalizedQuery", "test query");
        recordData.put("sessionId", "   ");

        MapRecord<String, Object, Object> record = createMockRecord("1234567890-0", recordData);

        doReturn(List.of(record))
                .doReturn(List.of())
                .when(streamOperations).read(
                        any(Consumer.class),
                        any(StreamReadOptions.class),
                        any(StreamOffset.class)
                );

        // When
        int result = flusher.flush();

        // Then
        assertThat(result).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SearchQuery>> captor = ArgumentCaptor.forClass(List.class);
        verify(logPort).saveBatch(captor.capture());
        SearchQuery saved = captor.getValue().get(0);
        assertThat(saved.sessionId()).isNull();
    }
}
