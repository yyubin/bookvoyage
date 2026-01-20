package org.yyubin.recommendation.graph.node;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserNode 테스트")
class UserNodeTest {

    @Test
    @DisplayName("Builder로 UserNode 생성")
    void build_Success() {
        // When
        UserNode node = UserNode.builder()
                .id(1L)
                .username("user")
                .email("user@example.com")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        // Then
        assertThat(node.getId()).isEqualTo(1L);
        assertThat(node.getUsername()).isEqualTo("user");
        assertThat(node.getEmail()).isEqualTo("user@example.com");
        assertThat(node.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    @DisplayName("Builder 기본값으로 빈 관계 Set 생성")
    void build_DefaultSets_Empty() {
        // When
        UserNode node = UserNode.builder().id(1L).build();

        // Then
        assertThat(node.getViewedBooks()).isNotNull().isEmpty();
        assertThat(node.getWishlistedBooks()).isNotNull().isEmpty();
        assertThat(node.getLikedReviewBooks()).isNotNull().isEmpty();
    }
}
