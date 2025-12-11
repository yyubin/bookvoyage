package org.yyubin.application.book.search;

import org.yyubin.application.book.search.dto.BookSearchPage;
import org.yyubin.application.book.search.query.SearchBooksQuery;

public interface SearchBooksUseCase {
    BookSearchPage query(SearchBooksQuery query);
}
