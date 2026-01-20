package org.yyubin.recommendation.graph.node;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GenreNode 테스트")
class GenreNodeTest {

    @Test
    @DisplayName("Builder로 GenreNode 생성")
    void build_Success() {
        // When
        GenreNode node = GenreNode.builder()
                .name("Fantasy")
                .description("Desc")
                .bookCount(5)
                .build();

        // Then
        assertThat(node.getName()).isEqualTo("Fantasy");
        assertThat(node.getDescription()).isEqualTo("Desc");
        assertThat(node.getBookCount()).isEqualTo(5);
    }
}
