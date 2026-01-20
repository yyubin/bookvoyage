package org.yyubin.recommendation.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ReviewSearchIndexEventDeserializer 테스트")
class ReviewSearchIndexEventDeserializerTest {

    private final ReviewSearchIndexEventDeserializer deserializer = new ReviewSearchIndexEventDeserializer();

    @Test
    @DisplayName("null 데이터면 null 반환")
    void deserialize_NullData_ReturnsNull() {
        // When
        ReviewSearchIndexEvent result = deserializer.deserialize("topic", null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 데이터면 null 반환")
    void deserialize_EmptyData_ReturnsNull() {
        // When
        ReviewSearchIndexEvent result = deserializer.deserialize("topic", new byte[0]);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("정상 JSON을 역직렬화한다")
    void deserialize_ValidJson_ReturnsEvent() {
        // Given
        String json = """
                {
                  "type": "UPSERT",
                  "reviewId": 1,
                  "userId": 2,
                  "bookId": 3,
                  "bookTitle": "Title",
                  "summary": "Summary",
                  "content": "Content",
                  "highlights": ["h1"],
                  "highlightsNorm": ["h1"],
                  "keywords": ["k1"],
                  "genre": "Fantasy",
                  "createdAt": "2024-01-02T03:04:05",
                  "rating": 5
                }
                """;

        // When
        ReviewSearchIndexEvent result = deserializer.deserialize("topic", json.getBytes(StandardCharsets.UTF_8));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo(ReviewSearchIndexEventType.UPSERT);
        assertThat(result.reviewId()).isEqualTo(1L);
        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.bookId()).isEqualTo(3L);
        assertThat(result.bookTitle()).isEqualTo("Title");
        assertThat(result.summary()).isEqualTo("Summary");
        assertThat(result.content()).isEqualTo("Content");
        assertThat(result.highlights()).containsExactly("h1");
        assertThat(result.highlightsNorm()).containsExactly("h1");
        assertThat(result.keywords()).containsExactly("k1");
        assertThat(result.genre()).isEqualTo("Fantasy");
        assertThat(result.createdAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 3, 4, 5));
        assertThat(result.rating()).isEqualTo(5);
    }

    @Test
    @DisplayName("잘못된 JSON이면 예외를 던진다")
    void deserialize_InvalidJson_ThrowsException() {
        // Given
        byte[] data = "not-json".getBytes(StandardCharsets.UTF_8);

        // When & Then
        assertThatThrownBy(() -> deserializer.deserialize("topic", data))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to deserialize ReviewSearchIndexEvent");
    }
}
