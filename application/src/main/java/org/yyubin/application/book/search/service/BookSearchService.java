package org.yyubin.application.book.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.book.search.SearchBooksUseCase;
import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.port.ExternalBookSearchPort;
import org.yyubin.application.book.search.query.PrintType;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.application.book.search.query.SearchOrder;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookSearchService implements SearchBooksUseCase {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 40;

    private final ExternalBookSearchPort externalBookSearchPort;

    @Override
    public BookSearchPage query(SearchBooksQuery query) {
        SearchBooksQuery normalized = normalize(query);
        ExternalBookSearchResult result = externalBookSearchPort.search(normalized);

        int next = normalized.startIndex() + normalized.size();
        Integer nextStartIndex = next < result.totalItems() ? next : null;

        return new BookSearchPage(result.items(), nextStartIndex, result.totalItems());
    }

    private SearchBooksQuery normalize(SearchBooksQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
        String keyword = query.keyword() == null ? "" : query.keyword().trim();
        if (keyword.isEmpty()) {
            throw new IllegalArgumentException("keyword must not be empty");
        }
        int startIndex = query.startIndex() == null ? 0 : Math.max(0, query.startIndex());
        int size = query.size() == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(MAX_PAGE_SIZE, query.size()));

        SearchOrder orderBy = query.orderBy() == null ? SearchOrder.RELEVANCE : query.orderBy();
        PrintType printType = query.printType() == null ? PrintType.ALL : query.printType();

        return new SearchBooksQuery(keyword, startIndex, size, query.language(), orderBy, printType);
    }
}
