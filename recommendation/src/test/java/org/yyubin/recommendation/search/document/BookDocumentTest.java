package org.yyubin.recommendation.search.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookDocument 테스트")
class BookDocumentTest {

    @Nested
    @DisplayName("BookDocument Builder")
    class BookDocumentBuilder {

        @Test
        @DisplayName("Builder로 모든 필드를 설정할 수 있다")
        void builder_AllFields() {
            // Given
            List<String> authors = List.of("Author1", "Author2");
            List<String> genres = List.of("Fiction", "Thriller");
            List<String> topics = List.of("Mystery", "Adventure");
            LocalDate publishedDate = LocalDate.of(2024, 1, 15);

            // When
            BookDocument document = BookDocument.builder()
                    .id("123")
                    .title("Test Book Title")
                    .description("Test book description")
                    .isbn("978-1234567890")
                    .authors(authors)
                    .genres(genres)
                    .topics(topics)
                    .publishedDate(publishedDate)
                    .viewCount(100)
                    .wishlistCount(50)
                    .reviewCount(25)
                    .averageRating(4.5f)
                    .searchableText("Test Book Title Test book description Author1 Author2")
                    .build();

            // Then
            assertThat(document.getId()).isEqualTo("123");
            assertThat(document.getTitle()).isEqualTo("Test Book Title");
            assertThat(document.getDescription()).isEqualTo("Test book description");
            assertThat(document.getIsbn()).isEqualTo("978-1234567890");
            assertThat(document.getAuthors()).containsExactly("Author1", "Author2");
            assertThat(document.getGenres()).containsExactly("Fiction", "Thriller");
            assertThat(document.getTopics()).containsExactly("Mystery", "Adventure");
            assertThat(document.getPublishedDate()).isEqualTo(publishedDate);
            assertThat(document.getViewCount()).isEqualTo(100);
            assertThat(document.getWishlistCount()).isEqualTo(50);
            assertThat(document.getReviewCount()).isEqualTo(25);
            assertThat(document.getAverageRating()).isEqualTo(4.5f);
            assertThat(document.getSearchableText()).isEqualTo("Test Book Title Test book description Author1 Author2");
        }

        @Test
        @DisplayName("Builder로 필수 필드만 설정할 수 있다")
        void builder_MinimalFields() {
            // When
            BookDocument document = BookDocument.builder()
                    .id("1")
                    .title("Minimal Book")
                    .build();

            // Then
            assertThat(document.getId()).isEqualTo("1");
            assertThat(document.getTitle()).isEqualTo("Minimal Book");
            assertThat(document.getDescription()).isNull();
            assertThat(document.getIsbn()).isNull();
            assertThat(document.getAuthors()).isNull();
            assertThat(document.getGenres()).isNull();
            assertThat(document.getTopics()).isNull();
            assertThat(document.getPublishedDate()).isNull();
            assertThat(document.getViewCount()).isNull();
            assertThat(document.getWishlistCount()).isNull();
            assertThat(document.getReviewCount()).isNull();
            assertThat(document.getAverageRating()).isNull();
        }

        @Test
        @DisplayName("NoArgsConstructor로 빈 객체를 생성할 수 있다")
        void noArgsConstructor() {
            // When
            BookDocument document = new BookDocument();

            // Then
            assertThat(document.getId()).isNull();
            assertThat(document.getTitle()).isNull();
        }

        @Test
        @DisplayName("AllArgsConstructor로 모든 필드를 설정할 수 있다")
        void allArgsConstructor() {
            // Given
            List<String> authors = List.of("Author");
            List<String> genres = List.of("Genre");
            List<String> topics = List.of("Topic");
            LocalDate publishedDate = LocalDate.now();

            // When
            BookDocument document = new BookDocument(
                    "1", "Title", "Desc", "ISBN",
                    authors, genres, topics,
                    publishedDate, 10, 5, 3, 4.0f, "searchable"
            );

            // Then
            assertThat(document.getId()).isEqualTo("1");
            assertThat(document.getTitle()).isEqualTo("Title");
            assertThat(document.getDescription()).isEqualTo("Desc");
        }
    }

    @Nested
    @DisplayName("BookDocument Setter")
    class BookDocumentSetter {

        @Test
        @DisplayName("Setter로 필드 값을 변경할 수 있다")
        void setter_ModifyFields() {
            // Given
            BookDocument document = new BookDocument();

            // When
            document.setId("999");
            document.setTitle("Updated Title");
            document.setDescription("Updated Description");
            document.setViewCount(500);

            // Then
            assertThat(document.getId()).isEqualTo("999");
            assertThat(document.getTitle()).isEqualTo("Updated Title");
            assertThat(document.getDescription()).isEqualTo("Updated Description");
            assertThat(document.getViewCount()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("buildSearchableText 메서드")
    class BuildSearchableText {

        @Test
        @DisplayName("title, description, authors가 모두 있을 때 연결된 문자열 반환")
        void buildSearchableText_AllFieldsPresent() {
            // Given
            String title = "Clean Code";
            String description = "A handbook of agile software craftsmanship";
            List<String> authors = List.of("Robert C. Martin", "Uncle Bob");

            // When
            String result = BookDocument.buildSearchableText(title, description, authors);

            // Then
            assertThat(result).isEqualTo("Clean Code A handbook of agile software craftsmanship Robert C. Martin Uncle Bob");
        }

        @Test
        @DisplayName("title만 있을 때 title만 반환")
        void buildSearchableText_OnlyTitle() {
            // When
            String result = BookDocument.buildSearchableText("Only Title", null, null);

            // Then
            assertThat(result).isEqualTo("Only Title");
        }

        @Test
        @DisplayName("description만 있을 때 description만 반환")
        void buildSearchableText_OnlyDescription() {
            // When
            String result = BookDocument.buildSearchableText(null, "Only Description", null);

            // Then
            assertThat(result).isEqualTo("Only Description");
        }

        @Test
        @DisplayName("authors만 있을 때 authors만 반환")
        void buildSearchableText_OnlyAuthors() {
            // Given
            List<String> authors = List.of("Author1", "Author2");

            // When
            String result = BookDocument.buildSearchableText(null, null, authors);

            // Then
            assertThat(result).isEqualTo("Author1 Author2");
        }

        @Test
        @DisplayName("모든 필드가 null일 때 빈 문자열 반환")
        void buildSearchableText_AllNull() {
            // When
            String result = BookDocument.buildSearchableText(null, null, null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 authors 리스트는 무시됨")
        void buildSearchableText_EmptyAuthorsList() {
            // When
            String result = BookDocument.buildSearchableText("Title", "Description", Collections.emptyList());

            // Then
            assertThat(result).isEqualTo("Title Description");
        }

        @Test
        @DisplayName("title과 authors만 있을 때")
        void buildSearchableText_TitleAndAuthors() {
            // Given
            List<String> authors = List.of("Author");

            // When
            String result = BookDocument.buildSearchableText("Title", null, authors);

            // Then
            assertThat(result).isEqualTo("Title Author");
        }

        @Test
        @DisplayName("description과 authors만 있을 때")
        void buildSearchableText_DescriptionAndAuthors() {
            // Given
            List<String> authors = List.of("Author");

            // When
            String result = BookDocument.buildSearchableText(null, "Description", authors);

            // Then
            assertThat(result).isEqualTo("Description Author");
        }

        @Test
        @DisplayName("title과 description만 있을 때")
        void buildSearchableText_TitleAndDescription() {
            // When
            String result = BookDocument.buildSearchableText("Title", "Description", null);

            // Then
            assertThat(result).isEqualTo("Title Description");
        }

        @Test
        @DisplayName("여러 저자가 공백으로 연결됨")
        void buildSearchableText_MultipleAuthors() {
            // Given
            List<String> authors = Arrays.asList("First Author", "Second Author", "Third Author");

            // When
            String result = BookDocument.buildSearchableText(null, null, authors);

            // Then
            assertThat(result).isEqualTo("First Author Second Author Third Author");
        }
    }

    @Nested
    @DisplayName("BookDocument equals/hashCode/toString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("동일한 필드를 가진 객체는 equals가 true")
        void equals_SameFields() {
            // Given
            BookDocument doc1 = BookDocument.builder()
                    .id("1")
                    .title("Same Title")
                    .viewCount(10)
                    .build();
            BookDocument doc2 = BookDocument.builder()
                    .id("1")
                    .title("Same Title")
                    .viewCount(10)
                    .build();

            // Then
            assertThat(doc1).isEqualTo(doc2);
            assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
        }

        @Test
        @DisplayName("다른 필드를 가진 객체는 equals가 false")
        void equals_DifferentFields() {
            // Given
            BookDocument doc1 = BookDocument.builder().id("1").title("Title1").build();
            BookDocument doc2 = BookDocument.builder().id("2").title("Title2").build();

            // Then
            assertThat(doc1).isNotEqualTo(doc2);
        }

        @Test
        @DisplayName("toString은 필드 정보를 포함")
        void toString_ContainsFieldInfo() {
            // Given
            BookDocument document = BookDocument.builder()
                    .id("123")
                    .title("Test Book")
                    .build();

            // When
            String result = document.toString();

            // Then
            assertThat(result).contains("BookDocument");
            assertThat(result).contains("id=123");
            assertThat(result).contains("title=Test Book");
        }
    }
}
