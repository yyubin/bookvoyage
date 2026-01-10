package org.yyubin.infrastructure.persistence.search;

import jakarta.persistence.*;
import lombok.*;
import org.yyubin.domain.search.SearchQuery;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "search_query_log",
    indexes = {
        @Index(name = "idx_normalized_query_created", columnList = "normalized_query, created_at"),
        @Index(name = "idx_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_session", columnList = "session_id, created_at")
    }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchQueryLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "query_text", nullable = false, length = 500)
    private String queryText;

    @Column(name = "normalized_query", nullable = false, length = 500)
    private String normalizedQuery;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "clicked_content_id")
    private Long clickedContentId;

    @Column(name = "clicked_content_type", length = 20)
    private String clickedContentType;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public SearchQuery toDomain() {
        return new SearchQuery(
            id,
            userId,
            sessionId,
            queryText,
            normalizedQuery,
            resultCount,
            clickedContentId,
            clickedContentType != null ? SearchQuery.ContentType.valueOf(clickedContentType) : null,
            source,
            createdAt
        );
    }

    public static SearchQueryLogEntity fromDomain(SearchQuery searchQuery) {
        return SearchQueryLogEntity.builder()
            .id(searchQuery.id())
            .userId(searchQuery.userId())
            .sessionId(searchQuery.sessionId())
            .queryText(searchQuery.queryText())
            .normalizedQuery(searchQuery.normalizedQuery())
            .resultCount(searchQuery.resultCount())
            .clickedContentId(searchQuery.clickedContentId())
            .clickedContentType(searchQuery.clickedContentType() != null ? searchQuery.clickedContentType().name() : null)
            .source(searchQuery.source())
            .createdAt(searchQuery.createdAt())
            .build();
    }
}
