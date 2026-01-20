package org.yyubin.recommendation.graph.node;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookNode 테스트")
class BookNodeTest {

    @Test
    @DisplayName("Builder로 BookNode 생성")
    void build_Success() {
        // Given
        AuthorNode author = AuthorNode.builder().id(10L).name("Author").build();
        GenreNode genre = GenreNode.builder().name("Fantasy").build();
        TopicNode topic = TopicNode.builder().name("Adventure").build();

        // When
        BookNode node = BookNode.builder()
                .id(1L)
                .title("Book")
                .isbn("ISBN")
                .description("Desc")
                .publishedDate(LocalDate.of(2024, 1, 2))
                .viewCount(10)
                .wishlistCount(2)
                .reviewCount(3)
                .authors(Set.of(author))
                .genres(Set.of(genre))
                .topics(Set.of(topic))
                .build();

        // Then
        assertThat(node.getId()).isEqualTo(1L);
        assertThat(node.getTitle()).isEqualTo("Book");
        assertThat(node.getIsbn()).isEqualTo("ISBN");
        assertThat(node.getDescription()).isEqualTo("Desc");
        assertThat(node.getPublishedDate()).isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(node.getViewCount()).isEqualTo(10);
        assertThat(node.getWishlistCount()).isEqualTo(2);
        assertThat(node.getReviewCount()).isEqualTo(3);
        assertThat(node.getAuthors()).containsExactly(author);
        assertThat(node.getGenres()).containsExactly(genre);
        assertThat(node.getTopics()).containsExactly(topic);
    }

    @Test
    @DisplayName("Builder 기본값으로 빈 관계 Set 생성")
    void build_DefaultSets_Empty() {
        // When
        BookNode node = BookNode.builder().id(1L).title("Book").build();

        // Then
        assertThat(node.getAuthors()).isNotNull().isEmpty();
        assertThat(node.getGenres()).isNotNull().isEmpty();
        assertThat(node.getTopics()).isNotNull().isEmpty();
    }
}
