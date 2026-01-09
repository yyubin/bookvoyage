package org.yyubin.infrastructure.recommendation.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.EmbeddingPort;
import org.yyubin.application.recommendation.port.out.SemanticCachePort;
import redis.clients.jedis.JedisPooled;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "ai.semantic-cache",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnBean(EmbeddingPort.class)
public class RedisSemanticCacheAdapter implements SemanticCachePort {

    private final JedisPooled jedis;
    private final EmbeddingPort embeddingPort;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final double similarityThreshold;
    private final int ttlSeconds;

    public RedisSemanticCacheAdapter(
        @Value("${spring.data.redis.host:localhost}") String redisHost,
        @Value("${spring.data.redis.port:6379}") int redisPort,
        @Value("${ai.semantic-cache.enabled:true}") boolean enabled,
        @Value("${ai.semantic-cache.similarity-threshold:0.1}") double similarityThreshold,
        @Value("${ai.semantic-cache.ttl:86400}") int ttlSeconds,
        EmbeddingPort embeddingPort,
        ObjectMapper objectMapper
    ) {
        this.jedis = new JedisPooled(redisHost, redisPort);
        this.embeddingPort = embeddingPort;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.similarityThreshold = similarityThreshold;
        this.ttlSeconds = ttlSeconds;

        log.info("Redis Semantic Cache Adapter created - Enabled: {}, TTL: {}s",
            enabled, ttlSeconds);
    }

    @PostConstruct
    @Override
    public void initialize() {
        if (!enabled) {
            log.info("Semantic cache is DISABLED - Skipping initialization");
            return;
        }

        try {
            // Redis 연결 테스트
            String pong = jedis.ping();
            log.info("Semantic Cache initialized successfully - Redis: {}", pong);

            // TODO: 추후 RediSearch Vector Search 인덱스 생성 추가
            // 현재는 간단한 key-value 캐싱 사용

        } catch (Exception e) {
            log.error("Failed to initialize Semantic Cache", e);
        }
    }

    @Override
    public Optional<String> get(String query, String category) {
        if (!enabled) {
            return Optional.empty();
        }

        try {
            String cacheKey = buildCacheKey(query, category);

            // Redis에서 캐시 조회
            String cachedResponse = jedis.get(cacheKey);

            if (cachedResponse != null) {
                log.info("Semantic Cache HIT - Category: {}", category);
                return Optional.of(cachedResponse);
            }

            log.debug("Semantic Cache MISS - Category: {}", category);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error checking semantic cache", e);
            return Optional.empty();
        }
    }

    @Override
    public void put(String query, String response, String category) {
        if (!enabled) {
            return;
        }

        try {
            String cacheKey = buildCacheKey(query, category);

            // Redis에 저장
            jedis.setex(cacheKey, ttlSeconds, response);

            log.debug("Semantic Cache stored - Category: {}, Key: {}", category, cacheKey);

        } catch (Exception e) {
            log.error("Error storing to semantic cache", e);
        }
    }

    private String buildCacheKey(String query, String category) {
        // 쿼리의 해시값을 사용하여 캐시 키 생성
        // TODO: 추후 Vector Similarity를 사용하여 유사한 쿼리도 캐시 히트되도록 개선
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(query.getBytes(StandardCharsets.UTF_8));
            String hashStr = Base64.getEncoder().encodeToString(hash).substring(0, 16);
            return String.format("semantic_cache:%s:%s", category, hashStr);
        } catch (Exception e) {
            log.error("Error building cache key", e);
            return String.format("semantic_cache:%s:%d", category, query.hashCode());
        }
    }
}
