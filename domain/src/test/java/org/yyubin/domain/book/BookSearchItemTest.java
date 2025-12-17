package org.yyubin.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BookSearchItem 도메인 테스트")
class BookSearchItemTest {

    @Nested
    @DisplayName("BookSearchItem 생성")
    class CreateBookSearchItem {

        @Test
        @DisplayName("유효한 데이터로 BookSearchItem을 생성할 수 있다")
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
            BookSearchItem item = BookSearchItem.of(
                    title, authors, isbn10, isbn13, coverUrl,
                    publisher, publishedDate, description, language,
                    pageCount, googleVolumeId
            );

            // then
            assertThat(item).isNotNull();
            assertThat(item.getTitle()).isEqualTo(title);
            assertThat(item.getAuthors()).isEqualTo(authors);
            assertThat(item.getIsbn10()).isEqualTo(isbn10);
            assertThat(item.getIsbn13()).isEqualTo(isbn13);
            assertThat(item.getCoverUrl()).isEqualTo(coverUrl);
            assertThat(item.getPublisher()).isEqualTo(publisher);
            assertThat(item.getPublishedDate()).isEqualTo(publishedDate);
            assertThat(item.getDescription()).isEqualTo(description);
            assertThat(item.getLanguage()).isEqualTo(language);
            assertThat(item.getPageCount()).isEqualTo(pageCount);
            assertThat(item.getGoogleVolumeId()).isEqualTo(googleVolumeId);
        }

        @Test
        @DisplayName("최소 필수 필드(title)만으로 BookSearchItem을 생성할 수 있다")
        void createWithMinimalRequiredFields() {
            // given
            String title = "Book Title";

            // when
            BookSearchItem item = BookSearchItem.of(
                    title, null, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(item).isNotNull();
            assertThat(item.getTitle()).isEqualTo(title);
            assertThat(item.getAuthors()).isEmpty(); // null authors는 빈 리스트로 변환
        }

        @Test
        @DisplayName("null title로 생성 시 예외가 발생한다")
        void nullTitleThrowsException() {
            // when & then
            assertThatThrownBy(() -> BookSearchItem.of(
                    null, List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            ))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("title must not be null");
        }

        @Test
        @DisplayName("null authors는 빈 리스트로 변환된다")
        void nullAuthorsConvertedToEmptyList() {
            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", null, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(item.getAuthors()).isNotNull();
            assertThat(item.getAuthors()).isEmpty();
        }

        @Test
        @DisplayName("빈 authors 리스트는 그대로 빈 리스트가 된다")
        void emptyAuthorsRemainsEmpty() {
            // given
            List<String> emptyAuthors = Collections.emptyList();

            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", emptyAuthors, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(item.getAuthors()).isEmpty();
        }

        @Test
        @DisplayName("여러 저자를 가진 BookSearchItem을 생성할 수 있다")
        void createWithMultipleAuthors() {
            // given
            List<String> authors = List.of("Author 1", "Author 2", "Author 3");

            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", authors, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(item.getAuthors()).hasSize(3);
            assertThat(item.getAuthors()).containsExactly("Author 1", "Author 2", "Author 3");
        }

        @Test
        @DisplayName("authors 리스트는 불변이다")
        void authorsListIsImmutable() {
            // given
            List<String> authors = Arrays.asList("Author 1", "Author 2");
            BookSearchItem item = BookSearchItem.of(
                    "Title", authors, null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThatThrownBy(() -> item.getAuthors().add("Author 3"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("원본 authors 리스트 변경이 BookSearchItem에 영향을 주지 않는다")
        void authorsListIsDefensiveCopy() {
            // given
            List<String> authors = Arrays.asList("Author 1", "Author 2");
            BookSearchItem item = BookSearchItem.of(
                    "Title", authors, null, null, null,
                    null, null, null, null, null, null
            );

            // when
            authors.set(0, "Modified Author");

            // then
            assertThat(item.getAuthors()).containsExactly("Author 1", "Author 2");
        }
    }

    @Nested
    @DisplayName("BookSearchItem 필드 접근")
    class BookSearchItemFieldAccess {

        @Test
        @DisplayName("모든 필드에 접근할 수 있다")
        void accessAllFields() {
            // given
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), "isbn10", "isbn13", "cover",
                    "publisher", "2024-01-01", "description", "ko", 300, "vol-123"
            );

            // when & then
            assertThat(item.getTitle()).isNotNull();
            assertThat(item.getAuthors()).isNotNull();
            assertThat(item.getIsbn10()).isNotNull();
            assertThat(item.getIsbn13()).isNotNull();
            assertThat(item.getCoverUrl()).isNotNull();
            assertThat(item.getPublisher()).isNotNull();
            assertThat(item.getPublishedDate()).isNotNull();
            assertThat(item.getDescription()).isNotNull();
            assertThat(item.getLanguage()).isNotNull();
            assertThat(item.getPageCount()).isNotNull();
            assertThat(item.getGoogleVolumeId()).isNotNull();
        }

        @Test
        @DisplayName("null 필드는 null로 유지된다 (title 제외)")
        void nullFieldsRemainNull() {
            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", null, null, null, null,
                    null, null, null, null, null, null
            );

            // then
            assertThat(item.getTitle()).isNotNull();
            assertThat(item.getAuthors()).isEmpty(); // null은 빈 리스트로
            assertThat(item.getIsbn10()).isNull();
            assertThat(item.getIsbn13()).isNull();
            assertThat(item.getCoverUrl()).isNull();
            assertThat(item.getPublisher()).isNull();
            assertThat(item.getPublishedDate()).isNull();
            assertThat(item.getDescription()).isNull();
            assertThat(item.getLanguage()).isNull();
            assertThat(item.getPageCount()).isNull();
            assertThat(item.getGoogleVolumeId()).isNull();
        }
    }

    @Nested
    @DisplayName("BookSearchItem 동등성")
    class BookSearchItemEquality {

        @Test
        @DisplayName("모든 필드가 같은 BookSearchItem은 동등하다")
        void equalItemsWithSameFields() {
            // given
            BookSearchItem item1 = BookSearchItem.of(
                    "Title", List.of("Author"), "isbn10", "isbn13", "cover",
                    "publisher", "2024-01-01", "description", "ko", 300, "vol"
            );
            BookSearchItem item2 = BookSearchItem.of(
                    "Title", List.of("Author"), "isbn10", "isbn13", "cover",
                    "publisher", "2024-01-01", "description", "ko", 300, "vol"
            );

            // when & then
            assertThat(item1).isEqualTo(item2);
            assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 BookSearchItem은 동등하지 않다")
        void notEqualItemsWithDifferentFields() {
            // given
            BookSearchItem item1 = BookSearchItem.of(
                    "Title 1", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );
            BookSearchItem item2 = BookSearchItem.of(
                    "Title 2", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(item1).isNotEqualTo(item2);
        }

        @Test
        @DisplayName("BookSearchItem은 자기 자신과 동등하다")
        void equalToItself() {
            // given
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(item).isEqualTo(item);
        }

        @Test
        @DisplayName("BookSearchItem은 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(item).isNotEqualTo(null);
        }

        @Test
        @DisplayName("authors 순서가 다르면 동등하지 않다")
        void notEqualWithDifferentAuthorOrder() {
            // given
            BookSearchItem item1 = BookSearchItem.of(
                    "Title", List.of("Author A", "Author B"), null, null, null,
                    null, null, null, null, null, null
            );
            BookSearchItem item2 = BookSearchItem.of(
                    "Title", List.of("Author B", "Author A"), null, null, null,
                    null, null, null, null, null, null
            );

            // when & then
            assertThat(item1).isNotEqualTo(item2);
        }
    }

    @Nested
    @DisplayName("BookSearchItem toString")
    class BookSearchItemToString {

        @Test
        @DisplayName("toString()은 BookSearchItem 정보를 포함한 문자열을 반환한다")
        void toStringContainsItemInfo() {
            // given
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), "isbn10", null, null,
                    null, null, null, null, null, "vol-123"
            );

            // when
            String result = item.toString();

            // then
            assertThat(result).contains("BookSearchItem");
            assertThat(result).contains("title=");
            assertThat(result).contains("authors=");
            assertThat(result).contains("googleVolumeId=");
        }
    }

    @Nested
    @DisplayName("BookSearchItem 특수 케이스")
    class BookSearchItemSpecialCases {

        @Test
        @DisplayName("GoogleVolumeId를 가진 BookSearchItem을 생성할 수 있다")
        void createWithGoogleVolumeId() {
            // given
            String googleVolumeId = "vol-abc123";

            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, null, googleVolumeId
            );

            // then
            assertThat(item.getGoogleVolumeId()).isEqualTo(googleVolumeId);
        }

        @Test
        @DisplayName("pageCount가 0인 BookSearchItem을 생성할 수 있다")
        void createWithZeroPageCount() {
            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, null, null, 0, null
            );

            // then
            assertThat(item.getPageCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("매우 긴 description을 가진 BookSearchItem을 생성할 수 있다")
        void createWithLongDescription() {
            // given
            String longDescription = "A".repeat(10000);

            // when
            BookSearchItem item = BookSearchItem.of(
                    "Title", List.of("Author"), null, null, null,
                    null, null, longDescription, null, null, null
            );

            // then
            assertThat(item.getDescription()).hasSize(10000);
        }
    }
}
