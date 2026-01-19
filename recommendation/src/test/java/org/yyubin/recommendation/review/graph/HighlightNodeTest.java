package org.yyubin.recommendation.review.graph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HighlightNode 테스트")
class HighlightNodeTest {

    @Test
    @DisplayName("AllArgsConstructor로 객체 생성")
    void constructor_AllArgs_CreatesObject() {
        // Given
        String normalizedValue = "normalized highlight";
        String rawValue = "Normalized Highlight";

        // When
        HighlightNode node = new HighlightNode(normalizedValue, rawValue);

        // Then
        assertThat(node.getNormalizedValue()).isEqualTo(normalizedValue);
        assertThat(node.getRawValue()).isEqualTo(rawValue);
    }

    @Test
    @DisplayName("NoArgsConstructor로 객체 생성")
    void constructor_NoArgs_CreatesEmptyObject() {
        // When
        HighlightNode node = new HighlightNode();

        // Then
        assertThat(node.getNormalizedValue()).isNull();
        assertThat(node.getRawValue()).isNull();
    }

    @Test
    @DisplayName("normalizedValue가 ID로 사용됨")
    void normalizedValue_UsedAsId() {
        // Given
        String normalizedValue = "unique-id-value";
        String rawValue = "Unique ID Value";

        // When
        HighlightNode node = new HighlightNode(normalizedValue, rawValue);

        // Then
        assertThat(node.getNormalizedValue()).isEqualTo(normalizedValue);
    }

    @Test
    @DisplayName("null 값으로 생성")
    void constructor_NullValues_CreatesObject() {
        // When
        HighlightNode node = new HighlightNode(null, null);

        // Then
        assertThat(node.getNormalizedValue()).isNull();
        assertThat(node.getRawValue()).isNull();
    }

    @Test
    @DisplayName("빈 문자열로 생성")
    void constructor_EmptyStrings_CreatesObject() {
        // Given
        String normalizedValue = "";
        String rawValue = "";

        // When
        HighlightNode node = new HighlightNode(normalizedValue, rawValue);

        // Then
        assertThat(node.getNormalizedValue()).isEmpty();
        assertThat(node.getRawValue()).isEmpty();
    }

    @Test
    @DisplayName("한글 하이라이트 저장")
    void constructor_KoreanHighlight_CreatesObject() {
        // Given
        String normalizedValue = "삶의 의미를 찾아서";
        String rawValue = "삶의 의미를 찾아서";

        // When
        HighlightNode node = new HighlightNode(normalizedValue, rawValue);

        // Then
        assertThat(node.getNormalizedValue()).isEqualTo(normalizedValue);
        assertThat(node.getRawValue()).isEqualTo(rawValue);
    }

    @Test
    @DisplayName("특수문자 포함 하이라이트 저장")
    void constructor_SpecialCharacters_CreatesObject() {
        // Given
        String normalizedValue = "quote: \"life is beautiful!\"";
        String rawValue = "Quote: \"Life is Beautiful!\"";

        // When
        HighlightNode node = new HighlightNode(normalizedValue, rawValue);

        // Then
        assertThat(node.getNormalizedValue()).isEqualTo(normalizedValue);
        assertThat(node.getRawValue()).isEqualTo(rawValue);
    }

    @Test
    @DisplayName("긴 텍스트 하이라이트 저장")
    void constructor_LongText_CreatesObject() {
        // Given
        String normalizedValue = "a".repeat(1000);
        String rawValue = "A".repeat(1000);

        // When
        HighlightNode node = new HighlightNode(normalizedValue, rawValue);

        // Then
        assertThat(node.getNormalizedValue()).hasSize(1000);
        assertThat(node.getRawValue()).hasSize(1000);
    }
}
