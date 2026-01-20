package org.yyubin.recommendation.graph.node;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TopicNode 테스트")
class TopicNodeTest {

    @Test
    @DisplayName("Builder로 TopicNode 생성")
    void build_Success() {
        // When
        TopicNode node = TopicNode.builder()
                .name("Adventure")
                .description("Desc")
                .bookCount(7)
                .build();

        // Then
        assertThat(node.getName()).isEqualTo("Adventure");
        assertThat(node.getDescription()).isEqualTo("Desc");
        assertThat(node.getBookCount()).isEqualTo(7);
    }
}
