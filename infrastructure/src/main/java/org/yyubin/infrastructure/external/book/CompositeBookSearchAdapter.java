package org.yyubin.infrastructure.external.book;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.port.ExternalBookSearchPort;
import org.yyubin.application.book.search.query.SearchBooksQuery;

/**
 * 멀티 소스 책 검색 어댑터
 * 카카오 API를 먼저 시도하고, 결과가 없으면 Google Books로 fallback
 */
@Slf4j
@Component
@Primary
public class CompositeBookSearchAdapter implements ExternalBookSearchPort {

    private final KakaoBooksSearchAdapter kakaoAdapter;
    private final GoogleBooksSearchAdapter googleAdapter;

    public CompositeBookSearchAdapter(
            KakaoBooksSearchAdapter kakaoAdapter,
            GoogleBooksSearchAdapter googleAdapter
    ) {
        this.kakaoAdapter = kakaoAdapter;
        this.googleAdapter = googleAdapter;
    }

    @Override
    public ExternalBookSearchResult search(SearchBooksQuery query) {
        // 1. 카카오 API 시도
        try {
            log.debug("Searching books via Kakao API: {}", query.keyword());
            ExternalBookSearchResult kakaoResult = kakaoAdapter.search(query);

            // 결과가 있으면 카카오 결과 반환
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
}
