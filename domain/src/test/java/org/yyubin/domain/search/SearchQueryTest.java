package org.yyubin.domain.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SearchQuery record tests")
class SearchQueryTest {

    @Test
    @DisplayName("of builds query with no click")
    void ofBuildsQuery() {
        SearchQuery query = SearchQuery.of(1L, "session", "query", "norm", 10, "source");

        assertThat(query.id()).isNull();
        assertThat(query.clickedContentId()).isNull();
        assertThat(query.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("withClick adds click info")
    void withClickAddsInfo() {
        SearchQuery query = SearchQuery.of(1L, "session", "query", "norm", 10, "source");
        SearchQuery updated = query.withClick(22L, SearchQuery.ContentType.BOOK);

        assertThat(updated.clickedContentId()).isEqualTo(22L);
        assertThat(updated.clickedContentType()).isEqualTo(SearchQuery.ContentType.BOOK);
        assertThat(updated.createdAt()).isEqualTo(query.createdAt());
    }
}
