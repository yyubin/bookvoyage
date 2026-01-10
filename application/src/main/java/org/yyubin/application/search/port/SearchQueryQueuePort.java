package org.yyubin.application.search.port;

import org.yyubin.domain.search.SearchQuery;

/**
 * Port for enqueueing search queries for batch persistence
 * <p>
 * This abstraction allows the Application layer to remain independent
 * of the specific queue implementation (e.g., Redis Stream, Redis List, etc.)
 */
public interface SearchQueryQueuePort {

    /**
     * Enqueue a search query for later batch persistence
     */
    void enqueue(SearchQuery searchQuery);
}
