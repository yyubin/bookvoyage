package org.yyubin.recommendation.review.highlight;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SimpleHighlightNormalizer 테스트")
class SimpleHighlightNormalizerTest {

    private SimpleHighlightNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new SimpleHighlightNormalizer();
    }

    @Test
    @DisplayName("null 입력 시 빈 문자열 반환")
    void normalize_NullInput_ReturnsEmptyString() {
        // When
        String result = normalizer.normalize(null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 문자열 입력 시 빈 문자열 반환")
    void normalize_EmptyString_ReturnsEmptyString() {
        // When
        String result = normalizer.normalize("");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("공백만 있는 입력 시 빈 문자열 반환")
    void normalize_OnlyWhitespace_ReturnsEmptyString() {
        // When
        String result = normalizer.normalize("   ");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대문자를 소문자로 변환")
    void normalize_UpperCase_ConvertsToLowerCase() {
        // Given
        String input = "HELLO WORLD";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    @DisplayName("앞뒤 공백 제거")
    void normalize_LeadingTrailingWhitespace_Trimmed() {
        // Given
        String input = "  hello world  ";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    @DisplayName("연속된 공백을 단일 공백으로 변환")
    void normalize_MultipleSpaces_ReplacedWithSingleSpace() {
        // Given
        String input = "hello    world";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    @DisplayName("탭과 줄바꿈을 단일 공백으로 변환")
    void normalize_TabsAndNewlines_ReplacedWithSingleSpace() {
        // Given
        String input = "hello\t\nworld";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("hello world");
    }

    @ParameterizedTest
    @CsvSource({
            "Hello World, hello world",
            "HELLO WORLD, hello world",
            "  Hello  World  , hello world"
    })
    @DisplayName("다양한 입력에 대한 정규화 테스트")
    void normalize_VariousInputs_ReturnsExpected(String input, String expected) {
        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("한글 텍스트 정규화")
    void normalize_KoreanText_NormalizesCorrectly() {
        // Given
        String input = "  삶의   의미를   찾아서  ";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("삶의 의미를 찾아서");
    }

    @Test
    @DisplayName("혼합 텍스트 (한글 + 영어) 정규화")
    void normalize_MixedKoreanEnglish_NormalizesCorrectly() {
        // Given
        String input = "  Hello  세상  World  ";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("hello 세상 world");
    }

    @Test
    @DisplayName("특수문자 포함 텍스트 정규화")
    void normalize_SpecialCharacters_PreservedExceptWhitespace() {
        // Given
        String input = "Hello! @#$% World?";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("hello! @#$% world?");
    }

    @Test
    @DisplayName("숫자 포함 텍스트 정규화")
    void normalize_WithNumbers_NumbersPreserved() {
        // Given
        String input = "Chapter  123  Section  456";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("chapter 123 section 456");
    }

    @Test
    @DisplayName("따옴표 포함 텍스트 정규화")
    void normalize_WithQuotes_QuotesPreserved() {
        // Given
        String input = "\"Life is  Beautiful\"";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("\"life is beautiful\"");
    }

    @Test
    @DisplayName("긴 텍스트 정규화")
    void normalize_LongText_NormalizesCorrectly() {
        // Given
        String input = "  This   is   a   very   long   text   with   multiple   spaces   ";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("this is a very long text with multiple spaces");
    }

    @Test
    @DisplayName("이미 정규화된 텍스트는 그대로 반환")
    void normalize_AlreadyNormalized_ReturnsUnchanged() {
        // Given
        String input = "already normalized text";

        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isEqualTo("already normalized text");
    }

    @Test
    @DisplayName("유니코드 공백 문자 처리 - Non-breaking space는 일반 공백으로 변환되지 않음")
    void normalize_UnicodeWhitespace_NotConverted() {
        // Given - Non-breaking space (\u00A0)는 \s+에 매칭되지 않음
        String input = "hello\u00A0world";

        // When
        String result = normalizer.normalize(input);

        // Then - Non-breaking space는 그대로 유지됨
        assertThat(result).isEqualTo("hello\u00A0world");
    }

    @Test
    @DisplayName("HighlightNormalizer 인터페이스 구현 확인")
    void normalizer_ImplementsHighlightNormalizer() {
        // Then
        assertThat(normalizer).isInstanceOf(org.yyubin.domain.review.HighlightNormalizer.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "A", "1", "가", "!", " a "})
    @DisplayName("단일 문자 입력 정규화")
    void normalize_SingleCharacter_NormalizesCorrectly(String input) {
        // When
        String result = normalizer.normalize(input);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(input.trim().toLowerCase().replaceAll("\\s+", " "));
    }
}
