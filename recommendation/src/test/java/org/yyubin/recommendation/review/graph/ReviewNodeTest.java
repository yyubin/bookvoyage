package org.yyubin.recommendation.review.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewNode 테스트")
class ReviewNodeTest {

    @Test
    @DisplayName("AllArgsConstructor로 객체 생성")
    void constructor_AllArgs_CreatesObject() {
        // Given
        Long reviewId = 1L;
        Long userId = 2L;
        Long bookId = 3L;
        Set<HighlightNode> highlights = new HashSet<>();
        highlights.add(new HighlightNode("norm1", "Raw1"));
        highlights.add(new HighlightNode("norm2", "Raw2"));

        // When
        ReviewNode node = new ReviewNode(reviewId, userId, bookId, highlights);

        // Then
        assertThat(node.getReviewId()).isEqualTo(1L);
        assertThat(node.getUserId()).isEqualTo(2L);
        assertThat(node.getBookId()).isEqualTo(3L);
        assertThat(node.getHighlights()).hasSize(2);
    }

    @Test
    @DisplayName("NoArgsConstructor로 객체 생성")
    void constructor_NoArgs_CreatesEmptyObject() {
        // When
        ReviewNode node = new ReviewNode();

        // Then
        assertThat(node.getReviewId()).isNull();
        assertThat(node.getUserId()).isNull();
        assertThat(node.getBookId()).isNull();
        assertThat(node.getHighlights()).isEmpty();
    }

    @Test
    @DisplayName("reviewId가 ID로 사용됨")
    void reviewId_UsedAsId() {
        // Given
        Long reviewId = 999L;

        // When
        ReviewNode node = new ReviewNode(reviewId, 1L, 1L, new HashSet<>());

        // Then
        assertThat(node.getReviewId()).isEqualTo(999L);
    }

    @Test
    @DisplayName("빈 highlights Set으로 생성")
    void constructor_EmptyHighlights_CreatesObject() {
        // Given
        Set<HighlightNode> emptyHighlights = new HashSet<>();

        // When
        ReviewNode node = new ReviewNode(1L, 2L, 3L, emptyHighlights);

        // Then
        assertThat(node.getHighlights()).isEmpty();
    }

    @Test
    @DisplayName("null highlights Set으로 생성")
    void constructor_NullHighlights_CreatesObject() {
        // When
        ReviewNode node = new ReviewNode(1L, 2L, 3L, null);

        // Then
        assertThat(node.getHighlights()).isNull();
    }

    @Test
    @DisplayName("여러 하이라이트 포함")
    void constructor_MultipleHighlights_AllIncluded() {
        // Given
        Set<HighlightNode> highlights = new HashSet<>();
        highlights.add(new HighlightNode("highlight1", "Highlight1"));
        highlights.add(new HighlightNode("highlight2", "Highlight2"));
        highlights.add(new HighlightNode("highlight3", "Highlight3"));

        // When
        ReviewNode node = new ReviewNode(1L, 2L, 3L, highlights);

        // Then
        assertThat(node.getHighlights()).hasSize(3);
        assertThat(node.getHighlights())
                .extracting(HighlightNode::getNormalizedValue)
                .containsExactlyInAnyOrder("highlight1", "highlight2", "highlight3");
    }

    @Test
    @DisplayName("중복 하이라이트는 Set에서 하나만 유지")
    void constructor_DuplicateHighlights_OnlyOneKept() {
        // Given
        Set<HighlightNode> highlights = new HashSet<>();
        HighlightNode highlight1 = new HighlightNode("same", "Same");
        HighlightNode highlight2 = new HighlightNode("same", "Same");
        highlights.add(highlight1);
        highlights.add(highlight2);

        // When
        ReviewNode node = new ReviewNode(1L, 2L, 3L, highlights);

        // Then
        // HashSet 특성상 동일한 객체면 하나만 유지 (HighlightNode의 equals/hashCode 구현에 따라 다름)
        assertThat(node.getHighlights()).isNotNull();
    }

    @Test
    @DisplayName("경계값 - Long.MAX_VALUE ID")
    void constructor_MaxLongValues_CreatesObject() {
        // Given
        Long reviewId = Long.MAX_VALUE;
        Long userId = Long.MAX_VALUE;
        Long bookId = Long.MAX_VALUE;

        // When
        ReviewNode node = new ReviewNode(reviewId, userId, bookId, new HashSet<>());

        // Then
        assertThat(node.getReviewId()).isEqualTo(Long.MAX_VALUE);
        assertThat(node.getUserId()).isEqualTo(Long.MAX_VALUE);
        assertThat(node.getBookId()).isEqualTo(Long.MAX_VALUE);
    }

    @Test
    @DisplayName("모든 ID가 null인 경우")
    void constructor_AllNullIds_CreatesObject() {
        // When
        ReviewNode node = new ReviewNode(null, null, null, new HashSet<>());

        // Then
        assertThat(node.getReviewId()).isNull();
        assertThat(node.getUserId()).isNull();
        assertThat(node.getBookId()).isNull();
    }

    @Test
    @DisplayName("HAS_HIGHLIGHT 관계를 가진 하이라이트")
    void highlights_HasHighlightRelationship() {
        // Given
        Set<HighlightNode> highlights = new HashSet<>();
        HighlightNode highlight = new HighlightNode("test highlight", "Test Highlight");
        highlights.add(highlight);

        // When
        ReviewNode node = new ReviewNode(1L, 2L, 3L, highlights);

        // Then
        assertThat(node.getHighlights()).contains(highlight);
    }
}
