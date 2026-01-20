package org.yyubin.recommendation.graph.node;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorNode 테스트")
class AuthorNodeTest {

    @Test
    @DisplayName("Builder로 AuthorNode 생성")
    void build_Success() {
        // When
        AuthorNode node = AuthorNode.builder()
                .id(1L)
                .name("Author")
                .biography("Bio")
                .bookCount(3)
                .build();

        // Then
        assertThat(node.getId()).isEqualTo(1L);
        assertThat(node.getName()).isEqualTo("Author");
        assertThat(node.getBiography()).isEqualTo("Bio");
        assertThat(node.getBookCount()).isEqualTo(3);
    }
}
