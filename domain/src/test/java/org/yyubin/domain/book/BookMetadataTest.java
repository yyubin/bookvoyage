package org.yyubin.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookMetadata 도메인 테스트")
class BookMetadataTest {

    @Nested
    @DisplayName("BookMetadata 생성")
    class CreateBookMetadata {

        @Test
        @DisplayName("유효한 데이터로 BookMetadata를 생성할 수 있다")
        void createWithValidData() {
            // given
            String title = "Clean Architecture";
            List<String> authors = List.of("Robert C. Martin");
            String isbn10 = "0134494164";
            String isbn13 = "9780134494166";
            String coverUrl = "https://example.com/cover.jpg";
            String publisher = "Prentice Hall";
            String publishedDate = "2017-09-20";
            String description = "A comprehensive guide";
            String language = "en";
            Integer pageCount = 432;
            String googleVolumeId = "vol-12345";

            // when
            BookMetadata metadata = BookMetadata.of(
                    title, authors, isbn10, isbn13, coverUrl,
                    publisher, publishedDate, description, language,
                    pageCount, googleVolumeId
            );

            // then
            assertThat(metadata).isNotNull();
            assertThat(metadata.getTitle()).isEqualTo(title);
            assertThat(metadata.getAuthors()).isEqualTo(authors);
            assertThat(metadata.getIsbn10()).isEqualTo(isbn10);
            assertThat(metadata.getIsbn13()).isEqualTo(isbn13);
            assertThat(metadata.getCoverUrl()).isEqualTo(coverUrl);
            assertThat(metadata.getPublisher()).isEqualTo(publisher);
            assertThat(metadata.getPublishedDate()).isEqualTo(publishedDate);
            assertThat(metadata.getDescription()).isEqualTo(description);
            assertThat(metadata.getLanguage()).isEqualTo(language);
            assertThat(metadata.getPageCount()).isEqualTo(pageCount);
            assertThat(metadata.getGoogleVolumeId()).isEqualTo(googleVolumeId);
        }

        @Test
        @DisplayName("최소 필수 필드만으로 BookMetadata를 생성할 수 있다")
        void createWithMinimalRequiredFields() {
            // given
            String title = "Book Title";
            List<String> authors = List.of("Author");

            // when
            BookMetadata metadata = BookMetadata.of(
                    title, authors, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(metadata).isNotNull();
            assertThat(metadata.getTitle()).isEqualTo(title);
            assertThat(metadata.getAuthors()).isEqualTo(authors);
            assertThat(metadata.getDescription()).isEmpty(); // null description은 빈 문자열로 변환
        }

        @Test
        @DisplayName("여러 저자를 가진 BookMetadata를 생성할 수 있다")
        void createWithMultipleAuthors() {
            // given
            List<String> authors = List.of("Author 1", "Author 2", "Author 3");

            // when
            BookMetadata metadata = BookMetadata.of(
                    "Title", authors, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(metadata.getAuthors()).hasSize(3);
            assertThat(metadata.getAuthors()).containsExactly("Author 1", "Author 2", "Author 3");
        }

        @Test
        @DisplayName("null description은 빈 문자열로 변환된다")
        void nullDescriptionConvertedToEmptyString() {
            // when
            BookMetadata metadata = BookMetadata.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(metadata.getDescription()).isNotNull();
            assertThat(metadata.getDescription()).isEmpty();
        }

        @Test
        @DisplayName("authors 리스트는 불변이다")
        void authorsListIsImmutable() {
            // given
            List<String> authors = Arrays.asList("Author 1", "Author 2");
            BookMetadata metadata = BookMetadata.of(
                    "Title", authors, null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThatThrownBy(() -> metadata.getAuthors().add("Author 3"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("BookMetadata 유효성 검증 - Title")
    class ValidateTitle {

        @Test
        @DisplayName("null 제목으로 생성 시 예외가 발생한다")
        void nullTitleThrowsException() {
            // when & then
            assertThatThrownBy(() -> BookMetadata.of(
                    null, List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book title cannot be empty");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n", "   \t\n   "})
        @DisplayName("빈 문자열 또는 공백만 있는 제목으로 생성 시 예외가 발생한다")
        void blankTitleThrowsException(String blankTitle) {
            // when & then
            assertThatThrownBy(() -> BookMetadata.of(
                    blankTitle, List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book title cannot be empty");
        }
    }

    @Nested
    @DisplayName("BookMetadata 유효성 검증 - Authors")
    class ValidateAuthors {

        @Test
        @DisplayName("null authors 리스트로 생성 시 예외가 발생한다")
        void nullAuthorsThrowsException() {
            // when & then
            assertThatThrownBy(() -> BookMetadata.of(
                    "Title", null, null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book authors cannot be empty");
        }

        @Test
        @DisplayName("빈 authors 리스트로 생성 시 예외가 발생한다")
        void emptyAuthorsListThrowsException() {
            // when & then
            assertThatThrownBy(() -> BookMetadata.of(
                    "Title", Collections.emptyList(), null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book authors cannot be empty");
        }

        @Test
        @DisplayName("null 요소만 있는 authors 리스트로 생성 시 예외가 발생한다")
        void authorsWithOnlyNullElementsThrowsException() {
            // given
            List<String> nullAuthors = Arrays.asList(null, null, null);

            // when & then
            assertThatThrownBy(() -> BookMetadata.of(
                    "Title", nullAuthors, null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book authors cannot be empty");
        }

        @Test
        @DisplayName("빈 문자열만 있는 authors 리스트로 생성 시 예외가 발생한다")
        void authorsWithOnlyEmptyStringsThrowsException() {
            // given
            List<String> emptyAuthors = List.of("", "  ", "\t");

            // when & then
            assertThatThrownBy(() -> BookMetadata.of(
                    "Title", emptyAuthors, null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Book authors cannot be empty");
        }

        @Test
        @DisplayName("null과 유효한 author가 섞인 리스트에서 null은 제거되고 유효한 author만 남는다")
        void authorsWithMixedNullAndValidValues() {
            // given
            List<String> mixedAuthors = Arrays.asList("Author 1", null, "Author 2", "", "  ", "Author 3");

            // when
            BookMetadata metadata = BookMetadata.of(
                    "Title", mixedAuthors, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(metadata.getAuthors()).hasSize(3);
            assertThat(metadata.getAuthors()).containsExactly("Author 1", "Author 2", "Author 3");
        }

        @Test
        @DisplayName("authors의 앞뒤 공백은 제거된다")
        void authorsTrimmed() {
            // given
            List<String> authorsWithWhitespace = List.of("  Author 1  ", "\tAuthor 2\n");

            // when
            BookMetadata metadata = BookMetadata.of(
                    "Title", authorsWithWhitespace, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(metadata.getAuthors()).containsExactly("Author 1", "Author 2");
        }
    }

    @Nested
    @DisplayName("BookMetadata 동등성")
    class BookMetadataEquality {

        @Test
        @DisplayName("모든 필드가 같은 BookMetadata는 동등하다")
        void equalMetadataWithSameFields() {
            // given
            BookMetadata metadata1 = BookMetadata.of(
                    "Title", List.of("Author"), "isbn10", "isbn13", "cover",
                    "publisher", "2024-01-01", "description", "ko", 300, "vol"
            );
            BookMetadata metadata2 = BookMetadata.of(
                    "Title", List.of("Author"), "isbn10", "isbn13", "cover",
                    "publisher", "2024-01-01", "description", "ko", 300, "vol"
            );

            // when & then
            assertThat(metadata1).isEqualTo(metadata2);
            assertThat(metadata1.hashCode()).isEqualTo(metadata2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 BookMetadata는 동등하지 않다")
        void notEqualMetadataWithDifferentFields() {
            // given
            BookMetadata metadata1 = BookMetadata.of(
                    "Title 1", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );
            BookMetadata metadata2 = BookMetadata.of(
                    "Title 2", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(metadata1).isNotEqualTo(metadata2);
        }

        @Test
        @DisplayName("BookMetadata는 자기 자신과 동등하다")
        void equalToItself() {
            // given
            BookMetadata metadata = BookMetadata.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(metadata).isEqualTo(metadata);
        }

        @Test
        @DisplayName("BookMetadata는 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            BookMetadata metadata = BookMetadata.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(metadata).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("BookMetadata toString")
    class BookMetadataToString {

        @Test
        @DisplayName("toString()은 BookMetadata 정보를 포함한 문자열을 반환한다")
        void toStringContainsMetadataInfo() {
            // given
            BookMetadata metadata = BookMetadata.of(
                    "Title", List.of("Author"), "isbn10", null, null,
                    null, null, null, null, null, null
            );

            // when
            String result = metadata.toString();

            // then
            assertThat(result).contains("BookMetadata");
            assertThat(result).contains("title=");
            assertThat(result).contains("authors=");
        }
    }
}
