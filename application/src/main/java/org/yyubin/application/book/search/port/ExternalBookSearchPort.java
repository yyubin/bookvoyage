package org.yyubin.application.book.search.port;

import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.query.SearchBooksQuery;

public interface ExternalBookSearchPort {
    ExternalBookSearchResult search(SearchBooksQuery query);
}
