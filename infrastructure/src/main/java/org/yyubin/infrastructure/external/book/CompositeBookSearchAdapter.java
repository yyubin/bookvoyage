package org.yyubin.infrastructure.external.book;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.port.ExternalBookSearchPort;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.infrastructure.external.book.dto.BookSearchCacheDto;

/**
 * 멀티 소스 책 검색 어댑터
 * - Redis 캐싱 적용 (TTL 1시간)
 * - 카카오 API를 먼저 시도하고, 결과가 없으면 Google Books로 fallback
 */
@Slf4j
@Component
@Primary
public class CompositeBookSearchAdapter implements ExternalBookSearchPort {

    private static final String CACHE_KEY_PREFIX = "book:search:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final KakaoBooksSearchAdapter kakaoAdapter;
    private final GoogleBooksSearchAdapter googleAdapter;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CompositeBookSearchAdapter(
            KakaoBooksSearchAdapter kakaoAdapter,
            GoogleBooksSearchAdapter googleAdapter,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.kakaoAdapter = kakaoAdapter;
        this.googleAdapter = googleAdapter;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public ExternalBookSearchResult search(SearchBooksQuery query) {
        String cacheKey = buildCacheKey(query);

        // 1. 캐시 조회
        ExternalBookSearchResult cached = getFromCache(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for query: {}", query.keyword());
            return cached;
        }

        // 2. 외부 API 호출
        ExternalBookSearchResult result = searchFromExternalApis(query);

        // 3. 결과가 있으면 캐시에 저장
        if (result.items() != null && !result.items().isEmpty()) {
            saveToCache(cacheKey, result);
        }

        return result;
    }

    private ExternalBookSearchResult searchFromExternalApis(SearchBooksQuery query) {
        // 1. 카카오 API 시도
        try {
            log.debug("Searching books via Kakao API: {}", query.keyword());
            ExternalBookSearchResult kakaoResult = kakaoAdapter.search(query);

            if (kakaoResult.items() != null && !kakaoResult.items().isEmpty()) {
                log.debug("Found {} books from Kakao API", kakaoResult.items().size());
                return kakaoResult;
            }

            log.debug("No results from Kakao API, falling back to Google Books");
        } catch (Exception e) {
            log.warn("Failed to search books via Kakao API, falling back to Google Books: {}", e.getMessage());
        }

        // 2. Google Books API로 fallback
        try {
            log.debug("Searching books via Google Books API: {}", query.keyword());
            ExternalBookSearchResult googleResult = googleAdapter.search(query);
            log.debug("Found {} books from Google Books API",
                    googleResult.items() != null ? googleResult.items().size() : 0);
            return googleResult;
        } catch (Exception e) {
            log.error("Failed to search books via Google Books API: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String buildCacheKey(SearchBooksQuery query) {
        return CACHE_KEY_PREFIX + query.keyword().toLowerCase().trim()
                + ":" + (query.startIndex() != null ? query.startIndex() : 0)
                + ":" + (query.size() != null ? query.size() : 10);
    }

    private ExternalBookSearchResult getFromCache(String cacheKey) {
        try {
            String json = redisTemplate.opsForValue().get(cacheKey);
            if (json != null) {
                BookSearchCacheDto dto = objectMapper.readValue(json, BookSearchCacheDto.class);
                return dto.toResult();
            }
        } catch (Exception e) {
            log.warn("Failed to read from cache: {}", e.getMessage());
        }
        return null;
    }

    private void saveToCache(String cacheKey, ExternalBookSearchResult result) {
        try {
            BookSearchCacheDto dto = BookSearchCacheDto.from(result);
            String json = objectMapper.writeValueAsString(dto);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
            log.debug("Cached search result for key: {}", cacheKey);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize cache: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Failed to save to cache: {}", e.getMessage());
        }
    }
}
