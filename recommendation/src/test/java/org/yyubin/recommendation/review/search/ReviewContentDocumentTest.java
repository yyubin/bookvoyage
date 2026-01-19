package org.yyubin.recommendation.review.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReviewContentDocument 테스트")
class ReviewContentDocumentTest {

    @Test
    @DisplayName("Builder로 모든 필드를 가진 객체 생성")
    void builder_AllFields_CreatesObject() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<String> highlights = List.of("highlight1", "highlight2");
        List<String> highlightsNorm = List.of("norm1", "norm2");
        List<String> keywords = List.of("keyword1", "keyword2");

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .userId(2L)
                .authorNickname("testAuthor")
                .bookId(3L)
                .bookTitle("Test Book Title")
                .summary("Test Summary")
                .content("Test Content")
                .highlights(highlights)
                .highlightsNorm(highlightsNorm)
                .keywords(keywords)
                .genre("Fiction")
                .createdAt(now)
                .rating(5)
                .build();

        // Then
        assertThat(document.getReviewId()).isEqualTo(1L);
        assertThat(document.getUserId()).isEqualTo(2L);
        assertThat(document.getAuthorNickname()).isEqualTo("testAuthor");
        assertThat(document.getBookId()).isEqualTo(3L);
        assertThat(document.getBookTitle()).isEqualTo("Test Book Title");
        assertThat(document.getSummary()).isEqualTo("Test Summary");
        assertThat(document.getContent()).isEqualTo("Test Content");
        assertThat(document.getHighlights()).containsExactly("highlight1", "highlight2");
        assertThat(document.getHighlightsNorm()).containsExactly("norm1", "norm2");
        assertThat(document.getKeywords()).containsExactly("keyword1", "keyword2");
        assertThat(document.getGenre()).isEqualTo("Fiction");
        assertThat(document.getCreatedAt()).isEqualTo(now);
        assertThat(document.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("Builder로 최소 필드만 가진 객체 생성")
    void builder_MinimalFields_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .build();

        // Then
        assertThat(document.getReviewId()).isEqualTo(1L);
        assertThat(document.getUserId()).isNull();
        assertThat(document.getBookId()).isNull();
        assertThat(document.getBookTitle()).isNull();
        assertThat(document.getSummary()).isNull();
        assertThat(document.getContent()).isNull();
        assertThat(document.getHighlights()).isNull();
        assertThat(document.getHighlightsNorm()).isNull();
        assertThat(document.getKeywords()).isNull();
        assertThat(document.getGenre()).isNull();
        assertThat(document.getCreatedAt()).isNull();
        assertThat(document.getRating()).isNull();
    }

    @Test
    @DisplayName("reviewId가 Document ID로 사용됨")
    void reviewId_UsedAsDocumentId() {
        // Given
        Long reviewId = 12345L;

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(reviewId)
                .build();

        // Then
        assertThat(document.getReviewId()).isEqualTo(reviewId);
    }

    @Test
    @DisplayName("빈 리스트 필드 처리")
    void builder_EmptyLists_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .highlights(List.of())
                .highlightsNorm(List.of())
                .keywords(List.of())
                .build();

        // Then
        assertThat(document.getHighlights()).isEmpty();
        assertThat(document.getHighlightsNorm()).isEmpty();
        assertThat(document.getKeywords()).isEmpty();
    }

    @Test
    @DisplayName("한글 텍스트 필드 저장")
    void builder_KoreanText_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .bookTitle("한글 책 제목")
                .summary("한글 요약입니다")
                .content("한글 내용입니다")
                .highlights(List.of("인상 깊은 구절"))
                .genre("문학")
                .build();

        // Then
        assertThat(document.getBookTitle()).isEqualTo("한글 책 제목");
        assertThat(document.getSummary()).isEqualTo("한글 요약입니다");
        assertThat(document.getContent()).isEqualTo("한글 내용입니다");
        assertThat(document.getHighlights()).containsExactly("인상 깊은 구절");
        assertThat(document.getGenre()).isEqualTo("문학");
    }

    @Test
    @DisplayName("rating 경계값 테스트 - 최소값")
    void builder_MinRating_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .rating(1)
                .build();

        // Then
        assertThat(document.getRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("rating 경계값 테스트 - 최대값")
    void builder_MaxRating_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .rating(5)
                .build();

        // Then
        assertThat(document.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("createdAt 날짜 시간 저장")
    void builder_CreatedAt_StoresDateTime() {
        // Given
        LocalDateTime specificTime = LocalDateTime.of(2024, 6, 15, 14, 30, 45);

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .createdAt(specificTime)
                .build();

        // Then
        assertThat(document.getCreatedAt()).isEqualTo(specificTime);
        assertThat(document.getCreatedAt().getYear()).isEqualTo(2024);
        assertThat(document.getCreatedAt().getMonthValue()).isEqualTo(6);
        assertThat(document.getCreatedAt().getDayOfMonth()).isEqualTo(15);
        assertThat(document.getCreatedAt().getHour()).isEqualTo(14);
        assertThat(document.getCreatedAt().getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("여러 하이라이트 저장")
    void builder_MultipleHighlights_AllStored() {
        // Given
        List<String> highlights = List.of("첫 번째 하이라이트", "두 번째 하이라이트", "세 번째 하이라이트");
        List<String> highlightsNorm = List.of("first", "second", "third");

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .highlights(highlights)
                .highlightsNorm(highlightsNorm)
                .build();

        // Then
        assertThat(document.getHighlights()).hasSize(3);
        assertThat(document.getHighlightsNorm()).hasSize(3);
    }

    @Test
    @DisplayName("여러 키워드 저장")
    void builder_MultipleKeywords_AllStored() {
        // Given
        List<String> keywords = List.of("실존주의", "철학", "자아");

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .keywords(keywords)
                .build();

        // Then
        assertThat(document.getKeywords()).containsExactly("실존주의", "철학", "자아");
    }

    @Test
    @DisplayName("긴 content 저장")
    void builder_LongContent_CreatesObject() {
        // Given
        String longContent = "Lorem ipsum ".repeat(1000);

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .content(longContent)
                .build();

        // Then
        assertThat(document.getContent()).hasSize(12000);
    }

    @Test
    @DisplayName("특수문자 포함 텍스트 저장")
    void builder_SpecialCharacters_CreatesObject() {
        // Given
        String specialContent = "Test \"quote\" with 'apostrophe' & <html> tags";

        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .content(specialContent)
                .summary(specialContent)
                .build();

        // Then
        assertThat(document.getContent()).isEqualTo(specialContent);
        assertThat(document.getSummary()).isEqualTo(specialContent);
    }

    @Test
    @DisplayName("authorNickname 저장")
    void builder_AuthorNickname_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(1L)
                .userId(100L)
                .authorNickname("책벌레123")
                .build();

        // Then
        assertThat(document.getAuthorNickname()).isEqualTo("책벌레123");
    }

    @Test
    @DisplayName("경계값 - Long.MAX_VALUE ID")
    void builder_MaxLongIds_CreatesObject() {
        // When
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(Long.MAX_VALUE)
                .userId(Long.MAX_VALUE)
                .bookId(Long.MAX_VALUE)
                .build();

        // Then
        assertThat(document.getReviewId()).isEqualTo(Long.MAX_VALUE);
        assertThat(document.getUserId()).isEqualTo(Long.MAX_VALUE);
        assertThat(document.getBookId()).isEqualTo(Long.MAX_VALUE);
    }
}
