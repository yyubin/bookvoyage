package org.yyubin.application.search.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SearchQueryNormalizer 테스트")
class SearchQueryNormalizerTest {

    private SearchQueryNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new SearchQueryNormalizer();
    }

    @Nested
    @DisplayName("null 및 빈 문자열 처리")
    class NullAndEmptyHandling {

        @Test
        @DisplayName("null 입력 시 빈 문자열 반환")
        void normalize_ReturnsEmptyStringForNull() {
            // When
            String result = normalizer.normalize(null);

            // Then
            assertThat(result).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n", "  \t\n  "})
        @DisplayName("빈 문자열 또는 공백만 있는 경우 빈 문자열 반환")
        void normalize_ReturnsEmptyStringForBlank(String input) {
            // When
            String result = normalizer.normalize(input);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("공백 처리")
    class WhitespaceHandling {

        @Test
        @DisplayName("앞뒤 공백 제거")
        void normalize_TrimsWhitespace() {
            // When
            String result = normalizer.normalize("  hello  ");

            // Then
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("여러 공백을 단일 공백으로 변환")
        void normalize_CollapsesMultipleSpaces() {
            // When
            String result = normalizer.normalize("hello    world");

            // Then
            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("탭과 줄바꿈을 공백으로 변환 후 축소")
        void normalize_HandlesTabsAndNewlines() {
            // When
            String result = normalizer.normalize("hello\t\nworld");

            // Then
            assertThat(result).isEqualTo("hello world");
        }
    }

    @Nested
    @DisplayName("대소문자 변환")
    class CaseConversion {

        @Test
        @DisplayName("대문자를 소문자로 변환")
        void normalize_ConvertsToLowercase() {
            // When
            String result = normalizer.normalize("HELLO WORLD");

            // Then
            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("혼합 대소문자를 소문자로 변환")
        void normalize_ConvertsMixedCaseToLowercase() {
            // When
            String result = normalizer.normalize("HeLLo WoRLd");

            // Then
            assertThat(result).isEqualTo("hello world");
        }
    }

    @Nested
    @DisplayName("특수문자 제거")
    class SpecialCharacterRemoval {

        @Test
        @DisplayName("특수문자 제거")
        void normalize_RemovesSpecialCharacters() {
            // When
            String result = normalizer.normalize("hello! @world# $test%");

            // Then
            assertThat(result).isEqualTo("hello world test");
        }

        @Test
        @DisplayName("구두점 제거")
        void normalize_RemovesPunctuation() {
            // When
            String result = normalizer.normalize("hello, world. test?");

            // Then
            assertThat(result).isEqualTo("hello world test");
        }

        @Test
        @DisplayName("괄호 및 따옴표 제거")
        void normalize_RemovesBracketsAndQuotes() {
            // When
            String result = normalizer.normalize("(hello) [world] \"test\" 'foo'");

            // Then
            assertThat(result).isEqualTo("hello world test foo");
        }

        @Test
        @DisplayName("숫자는 유지")
        void normalize_KeepsNumbers() {
            // When
            String result = normalizer.normalize("test123 hello456");

            // Then
            assertThat(result).isEqualTo("test123 hello456");
        }
    }

    @Nested
    @DisplayName("한국어 처리")
    class KoreanHandling {

        @Test
        @DisplayName("한글 유지")
        void normalize_KeepsKorean() {
            // When
            String result = normalizer.normalize("안녕하세요");

            // Then
            assertThat(result).isEqualTo("안녕하세요");
        }

        @Test
        @DisplayName("한글과 영어 혼합")
        void normalize_HandlesMixedKoreanEnglish() {
            // When
            String result = normalizer.normalize("Hello 세계");

            // Then
            assertThat(result).isEqualTo("hello 세계");
        }

        @Test
        @DisplayName("한글에서 특수문자 제거")
        void normalize_RemovesSpecialCharsFromKorean() {
            // When
            String result = normalizer.normalize("안녕!하세요? 테스트#입니다.");

            // Then
            assertThat(result).isEqualTo("안녕하세요 테스트입니다");
        }

        @Test
        @DisplayName("한글 공백 정규화")
        void normalize_NormalizesKoreanSpaces() {
            // When
            String result = normalizer.normalize("  안녕    하세요  ");

            // Then
            assertThat(result).isEqualTo("안녕 하세요");
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTests {

        @ParameterizedTest
        @CsvSource({
            "'  Hello World  ', 'hello world'",
            "'TEST123', 'test123'",
            "'hello!!!world', 'helloworld'",
            "'한글 English 123', '한글 english 123'",
            "'  Multiple   Spaces  ', 'multiple spaces'",
            "'@#$%special^&*chars', 'specialchars'"
        })
        @DisplayName("다양한 입력에 대한 정규화")
        void normalize_VariousInputs(String input, String expected) {
            // When
            String result = normalizer.normalize(input);

            // Then
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("실제 검색 쿼리 예시")
        void normalize_RealWorldSearchQuery() {
            // When
            String result = normalizer.normalize("  '해리포터'와 마법사의 돌!!  ");

            // Then
            assertThat(result).isEqualTo("해리포터와 마법사의 돌");
        }

        @Test
        @DisplayName("ISBN 형식 검색")
        void normalize_IsbnSearch() {
            // When
            String result = normalizer.normalize("978-89-123-4567-8");

            // Then
            assertThat(result).isEqualTo("9788912345678");
        }

        @Test
        @DisplayName("저자명 검색 정규화")
        void normalize_AuthorNameSearch() {
            // When
            String result = normalizer.normalize("  Robert C. Martin  ");

            // Then
            assertThat(result).isEqualTo("robert c martin");
        }
    }
}
