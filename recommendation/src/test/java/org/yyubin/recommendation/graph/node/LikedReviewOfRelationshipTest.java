package org.yyubin.recommendation.graph.node;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LikedReviewOfRelationship 테스트")
class LikedReviewOfRelationshipTest {

    @Test
    @DisplayName("Builder 기본값 확인")
    void build_DefaultValues() {
        // Given
        BookNode book = BookNode.builder().id(1L).title("Book").build();

        // When
        LikedReviewOfRelationship relationship = LikedReviewOfRelationship.builder()
                .book(book)
                .reviewId(10L)
                .likedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        // Then
        assertThat(relationship.getBook()).isEqualTo(book);
        assertThat(relationship.getReviewId()).isEqualTo(10L);
        assertThat(relationship.getLikedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(relationship.getLikeCount()).isEqualTo(1);
        assertThat(relationship.getWeight()).isEqualTo(0.15);
    }
}
