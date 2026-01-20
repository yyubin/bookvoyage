package org.yyubin.infrastructure.book.trending;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.book.dto.ShelfAdditionTrendResult;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShelfAdditionTrendCacheAdapter 테스트")
class ShelfAdditionTrendCacheAdapterTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private ShelfAdditionTrendCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        adapter = new ShelfAdditionTrendCacheAdapter(stringRedisTemplate, objectMapper);
        ReflectionTestUtils.setField(adapter, "ttlSeconds", 1800L);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("캐시 히트 시 결과를 반환한다")
    void get_CacheHit_ReturnsResult() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ZoneId timezone = ZoneId.of("Asia/Seoul");
        int limit = 20;

        ShelfAdditionTrendResult cached = new ShelfAdditionTrendResult(
            date,
            timezone.getId(),
            limit,
            List.of(),
            true,
            LocalDateTime.now()
        );
        String json = objectMapper.writeValueAsString(cached);

        when(valueOperations.get("book:trending:shelf-additions:2024-01-15:Asia/Seoul:20"))
            .thenReturn(json);

        // When
        Optional<ShelfAdditionTrendResult> result = adapter.get(date, timezone, limit);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().date()).isEqualTo(date);
        assertThat(result.get().limit()).isEqualTo(limit);
    }

    @Test
    @DisplayName("캐시 미스 시 빈 Optional 반환")
    void get_CacheMiss_ReturnsEmpty() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ZoneId timezone = ZoneId.of("Asia/Seoul");

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        Optional<ShelfAdditionTrendResult> result = adapter.get(date, timezone, 20);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("파싱 실패 시 빈 Optional 반환")
    void get_ParseFailure_ReturnsEmpty() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ZoneId timezone = ZoneId.of("Asia/Seoul");

        when(valueOperations.get(anyString())).thenReturn("invalid json");

        // When
        Optional<ShelfAdditionTrendResult> result = adapter.get(date, timezone, 20);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("결과를 캐시에 저장한다")
    void put_StoresInCache() throws Exception {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ZoneId timezone = ZoneId.of("Asia/Seoul");
        int limit = 20;

        ShelfAdditionTrendResult result = new ShelfAdditionTrendResult(
            date,
            timezone.getId(),
            limit,
            List.of(),
            false,
            LocalDateTime.now()
        );

        // When
        adapter.put(date, timezone, limit, result);

        // Then
        verify(valueOperations).set(
            eq("book:trending:shelf-additions:2024-01-15:Asia/Seoul:20"),
            anyString(),
            eq(Duration.ofSeconds(1800))
        );
    }

    @Test
    @DisplayName("캐시 키가 올바르게 생성된다")
    void cacheKey_GeneratedCorrectly() {
        // Given
        LocalDate date = LocalDate.of(2024, 6, 20);
        ZoneId timezone = ZoneId.of("America/New_York");
        int limit = 10;

        when(valueOperations.get("book:trending:shelf-additions:2024-06-20:America/New_York:10"))
            .thenReturn(null);

        // When
        adapter.get(date, timezone, limit);

        // Then
        verify(valueOperations).get("book:trending:shelf-additions:2024-06-20:America/New_York:10");
    }

    @Test
    @DisplayName("다른 limit은 다른 캐시 키를 사용한다")
    void cacheKey_DifferentLimits_DifferentKeys() {
        // Given
        LocalDate date = LocalDate.of(2024, 1, 15);
        ZoneId timezone = ZoneId.of("Asia/Seoul");

        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        adapter.get(date, timezone, 10);
        adapter.get(date, timezone, 20);

        // Then
        verify(valueOperations).get("book:trending:shelf-additions:2024-01-15:Asia/Seoul:10");
        verify(valueOperations).get("book:trending:shelf-additions:2024-01-15:Asia/Seoul:20");
    }
}
