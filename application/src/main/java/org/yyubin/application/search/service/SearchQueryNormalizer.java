package org.yyubin.application.search.service;

import org.springframework.stereotype.Component;

@Component
public class SearchQueryNormalizer {

    /**
     * Normalize search query for consistent trending tracking
     *
     * - Trim whitespace
     * - Convert to lowercase
     * - Remove special characters (except Korean, English, numbers, spaces)
     * - Collapse multiple spaces to single space
     */
    public String normalize(String query) {
        if (query == null || query.isBlank()) {
            return "";
        }

        return query.trim()
            .toLowerCase()
            .replaceAll("[^가-힣a-z0-9\\s]", "")  // Keep Korean, English, numbers, spaces
            .replaceAll("\\s+", " ")              // Collapse multiple spaces
            .trim();
    }
}
