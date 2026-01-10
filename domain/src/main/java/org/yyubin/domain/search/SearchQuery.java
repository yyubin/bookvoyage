package org.yyubin.domain.search;

import java.time.LocalDateTime;

public record SearchQuery(
    Long id,
    Long userId,
    String sessionId,
    String queryText,
    String normalizedQuery,
    Integer resultCount,
    Long clickedContentId,
    ContentType clickedContentType,
    String source,
    LocalDateTime createdAt
) {
    public static SearchQuery of(
        Long userId,
        String sessionId,
        String queryText,
        String normalizedQuery,
        Integer resultCount,
        String source
    ) {
        return new SearchQuery(
            null,
            userId,
            sessionId,
            queryText,
            normalizedQuery,
            resultCount,
            null,
            null,
            source,
            LocalDateTime.now()
        );
    }

    public SearchQuery withClick(Long contentId, ContentType contentType) {
        return new SearchQuery(
            id,
            userId,
            sessionId,
            queryText,
            normalizedQuery,
            resultCount,
            contentId,
            contentType,
            source,
            createdAt
        );
    }

    public enum ContentType {
        BOOK,
        REVIEW
    }
}
