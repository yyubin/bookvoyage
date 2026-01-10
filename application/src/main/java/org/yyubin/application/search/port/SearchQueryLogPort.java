package org.yyubin.application.search.port;

import org.yyubin.domain.search.SearchQuery;

import java.util.List;

public interface SearchQueryLogPort {
    void save(SearchQuery searchQuery);
    void saveBatch(List<SearchQuery> searchQueries);
}
