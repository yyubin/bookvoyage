package org.yyubin.domain.review;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Keyword 도메인 테스트")
class KeywordTest {

    private static class TestKeywordNormalizer implements KeywordNormalizer {
        @Override
        public String normalize(String raw) {
            return raw.toLowerCase().trim();
        }
    }

    @Nested
    @DisplayName("Keyword 생성 - create 메서드")
    class CreateKeywordWithCreate {

        @Test
        @DisplayName("유효한 데이터로 Keyword를 생성할 수 있다")
        void createWithValidData() {
            // given
            String rawValue = "Java";
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            assertThat(keyword).isNotNull();
            assertThat(keyword.getId()).isNull();
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo("java");
            assertThat(keyword.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("create 메서드는 현재 시간을 createdAt으로 설정한다")
        void createSetsCurrentTime() {
            // given
            String rawValue = "Java";
            KeywordNormalizer normalizer = new TestKeywordNormalizer();
            LocalDateTime before = LocalDateTime.now();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            LocalDateTime after = LocalDateTime.now();
            assertThat(keyword.getCreatedAt()).isAfterOrEqualTo(before);
            assertThat(keyword.getCreatedAt()).isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("normalizer가 rawValue를 정규화한 값이 저장된다")
        void normalizerProcessesRawValue() {
            // given
            String rawValue = "  JAVA  ";
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo("java");
        }

        @Test
        @DisplayName("null normalizer로 생성 시 예외가 발생한다")
        void createWithNullNormalizer() {
            // given
            String rawValue = "Java";

            // when & then
            assertThatThrownBy(() -> Keyword.create(rawValue, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Keyword normalizer cannot be null");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("null, 빈 문자열, 또는 공백만 있는 rawValue로 생성 시 예외가 발생한다")
        void createWithBlankRawValue(String blankValue) {
            // given
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when & then
            assertThatThrownBy(() -> Keyword.create(blankValue, normalizer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Keyword value cannot be empty");
        }
    }

    @Nested
    @DisplayName("Keyword 생성 - of 메서드")
    class CreateKeywordWithOf {

        @Test
        @DisplayName("모든 필드를 지정하여 Keyword를 생성할 수 있다")
        void createWithAllFields() {
            // given
            KeywordId id = new KeywordId(1L);
            String rawValue = "Java";
            String normalizedValue = "java";
            LocalDateTime createdAt = LocalDateTime.now();

            // when
            Keyword keyword = Keyword.of(id, rawValue, normalizedValue, createdAt);

            // then
            assertThat(keyword).isNotNull();
            assertThat(keyword.getId()).isEqualTo(id);
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo(normalizedValue);
            assertThat(keyword.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("null id로 생성 시 예외가 발생한다")
        void createWithNullId() {
            // when & then
            assertThatThrownBy(() -> Keyword.of(null, "Java", "java", LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Keyword ID cannot be null");
        }

        @Test
        @DisplayName("null rawValue로 생성 시 예외가 발생한다")
        void createWithNullRawValue() {
            // when & then
            assertThatThrownBy(() -> Keyword.of(new KeywordId(1L), null, "java", LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Raw value cannot be null");
        }

        @Test
        @DisplayName("null normalizedValue로 생성 시 예외가 발생한다")
        void createWithNullNormalizedValue() {
            // when & then
            assertThatThrownBy(() -> Keyword.of(new KeywordId(1L), "Java", null, LocalDateTime.now()))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Normalized value cannot be null");
        }

        @Test
        @DisplayName("null createdAt로 생성 시 예외가 발생한다")
        void createWithNullCreatedAt() {
            // when & then
            assertThatThrownBy(() -> Keyword.of(new KeywordId(1L), "Java", "java", null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Created at cannot be null");
        }
    }

    @Nested
    @DisplayName("Keyword withId 메서드")
    class KeywordWithId {

        @Test
        @DisplayName("withId 메서드로 ID를 설정한 새로운 Keyword를 생성할 수 있다")
        void withIdCreatesNewKeywordWithId() {
            // given
            KeywordNormalizer normalizer = new TestKeywordNormalizer();
            Keyword keyword = Keyword.create("Java", normalizer);
            Long newId = 100L;

            // when
            Keyword keywordWithId = keyword.withId(newId);

            // then
            assertThat(keywordWithId).isNotNull();
            assertThat(keywordWithId.getId()).isEqualTo(new KeywordId(newId));
            assertThat(keywordWithId.getRawValue()).isEqualTo(keyword.getRawValue());
            assertThat(keywordWithId.getNormalizedValue()).isEqualTo(keyword.getNormalizedValue());
            assertThat(keywordWithId.getCreatedAt()).isEqualTo(keyword.getCreatedAt());
        }

        @Test
        @DisplayName("withId는 불변성을 유지하며 원본 Keyword는 변경되지 않는다")
        void withIdDoesNotModifyOriginal() {
            // given
            KeywordNormalizer normalizer = new TestKeywordNormalizer();
            Keyword original = Keyword.create("Java", normalizer);
            KeywordId originalId = original.getId();

            // when
            Keyword modified = original.withId(100L);

            // then
            assertThat(original.getId()).isEqualTo(originalId);
            assertThat(modified.getId()).isNotEqualTo(originalId);
        }
    }

    @Nested
    @DisplayName("Keyword 필드 접근")
    class KeywordFieldAccess {

        @Test
        @DisplayName("모든 getter 메서드로 필드에 접근할 수 있다")
        void accessAllFields() {
            // given
            KeywordId id = new KeywordId(1L);
            String rawValue = "Java Programming";
            String normalizedValue = "java programming";
            LocalDateTime createdAt = LocalDateTime.now();
            Keyword keyword = Keyword.of(id, rawValue, normalizedValue, createdAt);

            // when & then
            assertThat(keyword.getId()).isEqualTo(id);
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo(normalizedValue);
            assertThat(keyword.getCreatedAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("Keyword 특수 케이스")
    class KeywordSpecialCases {

        @Test
        @DisplayName("특수 문자를 포함한 rawValue로 Keyword를 생성할 수 있다")
        void createWithSpecialCharacters() {
            // given
            String rawValue = "C++";
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo("c++");
        }

        @Test
        @DisplayName("한글 rawValue로 Keyword를 생성할 수 있다")
        void createWithKoreanCharacters() {
            // given
            String rawValue = "자바";
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo("자바");
        }

        @Test
        @DisplayName("공백이 포함된 rawValue로 Keyword를 생성할 수 있다")
        void createWithWhitespace() {
            // given
            String rawValue = "Java Programming";
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            assertThat(keyword.getRawValue()).isEqualTo(rawValue);
            assertThat(keyword.getNormalizedValue()).isEqualTo("java programming");
        }

        @Test
        @DisplayName("매우 긴 rawValue로 Keyword를 생성할 수 있다")
        void createWithLongRawValue() {
            // given
            String rawValue = "a".repeat(1000);
            KeywordNormalizer normalizer = new TestKeywordNormalizer();

            // when
            Keyword keyword = Keyword.create(rawValue, normalizer);

            // then
            assertThat(keyword.getRawValue()).hasSize(1000);
        }
    }
}
