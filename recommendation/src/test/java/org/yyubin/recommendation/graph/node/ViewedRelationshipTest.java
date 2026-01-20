package org.yyubin.recommendation.graph.node;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ViewedRelationship 테스트")
class ViewedRelationshipTest {

    @Test
    @DisplayName("Builder 기본값 확인")
    void build_DefaultValues() {
        // Given
        BookNode book = BookNode.builder().id(1L).title("Book").build();

        // When
        ViewedRelationship relationship = ViewedRelationship.builder()
                .book(book)
                .firstViewedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .lastViewedAt(LocalDateTime.of(2024, 1, 2, 12, 0))
                .build();

        // Then
        assertThat(relationship.getBook()).isEqualTo(book);
        assertThat(relationship.getFirstViewedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(relationship.getLastViewedAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 12, 0));
        assertThat(relationship.getViewCount()).isEqualTo(1);
        assertThat(relationship.getTotalDwellTimeSeconds()).isEqualTo(0L);
        assertThat(relationship.getWeight()).isEqualTo(0.05);
    }
}
