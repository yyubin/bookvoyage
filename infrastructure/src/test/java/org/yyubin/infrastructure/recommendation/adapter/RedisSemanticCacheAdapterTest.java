package org.yyubin.infrastructure.recommendation.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.yyubin.application.recommendation.port.out.EmbeddingPort;
import redis.clients.jedis.JedisPooled;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisSemanticCacheAdapter 테스트")
class RedisSemanticCacheAdapterTest {

    @Mock
    private JedisPooled jedis;

    @Mock
    private EmbeddingPort embeddingPort;

    private ObjectMapper objectMapper;

    private RedisSemanticCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        adapter = createAdapter(true);
    }

    private RedisSemanticCacheAdapter createAdapter(boolean enabled) {
        RedisSemanticCacheAdapter adapterInstance = new RedisSemanticCacheAdapter(
            "localhost",
            6379,
            enabled,
            0.1,
            86400,
            embeddingPort,
            objectMapper
        );
        ReflectionTestUtils.setField(adapterInstance, "jedis", jedis);
        return adapterInstance;
    }

    @Test
    @DisplayName("캐시 히트 시 저장된 값을 반환한다")
    void get_CacheHit_ReturnsCachedValue() {
        // Given
        String query = "recommend a book";
        String category = "book_recommendation";
        String cachedResponse = "Here is my recommendation";

        when(jedis.get(anyString())).thenReturn(cachedResponse);

        // When
        Optional<String> result = adapter.get(query, category);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(cachedResponse);
    }

    @Test
    @DisplayName("캐시 미스 시 빈 Optional을 반환한다")
    void get_CacheMiss_ReturnsEmpty() {
        // Given
        String query = "recommend a book";
        String category = "book_recommendation";

        when(jedis.get(anyString())).thenReturn(null);

        // When
        Optional<String> result = adapter.get(query, category);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("캐시 비활성화 시 빈 Optional을 반환한다")
    void get_Disabled_ReturnsEmpty() {
        // Given
        adapter = createAdapter(false);
        String query = "recommend a book";
        String category = "book_recommendation";

        // When
        Optional<String> result = adapter.get(query, category);

        // Then
        assertThat(result).isEmpty();
        verify(jedis, never()).get(anyString());
    }

    @Test
    @DisplayName("put 호출 시 Redis에 값을 저장한다")
    void put_StoresValueInRedis() {
        // Given
        String query = "recommend a book";
        String response = "Here is my recommendation";
        String category = "book_recommendation";

        // When
        adapter.put(query, response, category);

        // Then
        verify(jedis).setex(anyString(), eq(86400L), eq(response));
    }

    @Test
    @DisplayName("캐시 비활성화 시 put이 저장하지 않는다")
    void put_Disabled_DoesNotStore() {
        // Given
        adapter = createAdapter(false);
        String query = "recommend a book";
        String response = "Here is my recommendation";
        String category = "book_recommendation";

        // When
        adapter.put(query, response, category);

        // Then
        verify(jedis, never()).setex(anyString(), anyInt(), anyString());
    }

    @Test
    @DisplayName("initialize 호출 시 Redis 연결을 테스트한다")
    void initialize_PingsRedis() {
        // Given
        when(jedis.ping()).thenReturn("PONG");

        // When
        adapter.initialize();

        // Then
        verify(jedis).ping();
    }

    @Test
    @DisplayName("캐시 비활성화 시 initialize가 Redis에 연결하지 않는다")
    void initialize_Disabled_DoesNotPingRedis() {
        // Given
        adapter = createAdapter(false);

        // When
        adapter.initialize();

        // Then
        verify(jedis, never()).ping();
    }

    @Test
    @DisplayName("Redis 예외 발생 시 get은 빈 Optional을 반환한다")
    void get_OnException_ReturnsEmpty() {
        // Given
        String query = "test query";
        String category = "test";
        when(jedis.get(anyString())).thenThrow(new RuntimeException("Redis error"));

        // When
        Optional<String> result = adapter.get(query, category);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("같은 쿼리와 카테고리는 같은 캐시 키를 생성한다")
    void buildCacheKey_SameInputs_SameKey() {
        // Given
        String query = "recommend a book";
        String category = "books";

        when(jedis.get(anyString())).thenReturn(null);

        // When
        adapter.get(query, category);
        adapter.get(query, category);

        // Then - 같은 키로 두 번 조회
        verify(jedis, org.mockito.Mockito.times(2)).get(anyString());
    }
}
