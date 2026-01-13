package org.yyubin.infrastructure.external.book;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.port.ExternalBookSearchPort;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksDocumentResponse;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.KakaoBooksSearchResponse;

@Component
@RequiredArgsConstructor
public class KakaoBooksSearchAdapter implements ExternalBookSearchPort {

    private final KakaoBooksClient client;
    private final KakaoBooksProperties properties;

    @Override
    public ExternalBookSearchResult search(SearchBooksQuery query) {
        // 카카오 API는 페이지 기반 (1-indexed)이므로 startIndex를 페이지 번호로 변환
        int page = (query.startIndex() / query.size()) + 1;

        KakaoBooksSearchRequest request = new KakaoBooksSearchRequest(
                query.keyword(),
                null, // sort - accuracy가 기본값
                page,
                Math.min(query.size(), properties.getMaxResults()),
                null  // target - 전체 검색
        );

        KakaoBooksSearchResponse response = client.search(request);
        List<BookSearchItem> items = mapItems(response);
        int totalItems = response != null && response.meta() != null ? response.meta().totalCount() : 0;

        return new ExternalBookSearchResult(items, totalItems);
    }

    private List<BookSearchItem> mapItems(KakaoBooksSearchResponse response) {
        if (response == null || response.documents() == null) {
            return Collections.emptyList();
        }

        return response.documents().stream()
                .filter(doc -> doc.title() != null && !doc.title().isBlank())
                .map(this::toDomain)
                .toList();
    }

    private BookSearchItem toDomain(KakaoBooksDocumentResponse doc) {
        IsbnPair isbnPair = parseIsbn(doc.isbn());

        return BookSearchItem.of(
                doc.title(),
                doc.authors(),
                isbnPair.isbn10(),
                isbnPair.isbn13(),
                doc.thumbnail(),
                doc.publisher(),
                doc.datetime(),
                doc.contents(),
                null, // 카카오 API는 언어 정보를 제공하지 않음
                null, // 카카오 API는 페이지 수를 제공하지 않음
                null  // googleVolumeId는 null
        );
    }

    /**
     * 카카오 API의 ISBN 필드는 "ISBN10 ISBN13" 형식 또는 둘 중 하나만 포함
     * 예: "8936433520 9788936433529" 또는 "9788936433529"
     */
    private IsbnPair parseIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) {
            return new IsbnPair(null, null);
        }

        String[] parts = isbn.trim().split("\\s+");
        if (parts.length == 2) {
            // 두 개의 ISBN이 있는 경우
            String first = parts[0];
            String second = parts[1];

            // ISBN-10은 10자리, ISBN-13은 13자리
            if (first.length() == 10 && second.length() == 13) {
                return new IsbnPair(first, second);
            } else if (first.length() == 13 && second.length() == 10) {
                return new IsbnPair(second, first);
            }
        } else if (parts.length == 1) {
            // 하나의 ISBN만 있는 경우
            String single = parts[0];
            if (single.length() == 10) {
                return new IsbnPair(single, null);
            } else if (single.length() == 13) {
                return new IsbnPair(null, single);
            }
        }

        return new IsbnPair(null, null);
    }

    private record IsbnPair(String isbn10, String isbn13) {
    }
}
