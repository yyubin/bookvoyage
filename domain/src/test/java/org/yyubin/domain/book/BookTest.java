package org.yyubin.domain.book;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Book 도메인 테스트")
class BookTest {

    private BookMetadata createSampleMetadata() {
        return BookMetadata.of(
                "Clean Architecture",
                List.of("Robert C. Martin"),
                "0134494164",
                "9780134494166",
                "https://example.com/cover.jpg",
                "Prentice Hall",
                "2017-09-20",
                "A comprehensive guide to software architecture",
                "en",
                432,
                "vol-12345"
        );
    }

    @Nested
    @DisplayName("Book 생성")
    class CreateBook {

        @Test
        @DisplayName("ID와 메타데이터로 Book을 생성할 수 있다")
        void createWithIdAndMetadata() {
            // given
            BookId bookId = BookId.of(1L);
            BookMetadata metadata = createSampleMetadata();

            // when
            Book book = Book.of(bookId, metadata);

            // then
            assertThat(book).isNotNull();
            assertThat(book.getId()).isEqualTo(bookId);
            assertThat(book.getMetadata()).isEqualTo(metadata);
        }

        @Test
        @DisplayName("null ID로 Book을 생성할 수 있다 (새로 생성되는 Book)")
        void createWithNullId() {
            // given
            BookMetadata metadata = createSampleMetadata();

            // when
            Book book = Book.of(null, metadata);

            // then
            assertThat(book).isNotNull();
            assertThat(book.getId()).isNull();
            assertThat(book.getMetadata()).isEqualTo(metadata);
        }

        @Test
        @DisplayName("null 메타데이터로 Book 생성 시 예외가 발생한다")
        void createWithNullMetadata() {
            // given
            BookId bookId = BookId.of(1L);

            // when & then
            assertThatThrownBy(() -> Book.of(bookId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Book metadata cannot be null");
        }

        @Test
        @DisplayName("create 메서드로 Book을 생성할 수 있다")
        void createWithStaticFactoryMethod() {
            // when
            Book book = Book.create(
                    "Clean Code",
                    List.of("Robert C. Martin"),
                    "0132350882",
                    "9780132350884",
                    "https://example.com/cleancode.jpg",
                    "Prentice Hall",
                    "2008-08-01",
                    "A handbook of agile software craftsmanship",
                    "en",
                    464,
                    "vol-67890"
            );

            // then
            assertThat(book).isNotNull();
            assertThat(book.getId()).isNull(); // 새로 생성되는 Book은 ID가 null
            assertThat(book.getMetadata()).isNotNull();
            assertThat(book.getMetadata().getTitle()).isEqualTo("Clean Code");
            assertThat(book.getMetadata().getAuthors()).containsExactly("Robert C. Martin");
        }
    }

    @Nested
    @DisplayName("Book 업데이트")
    class UpdateBook {

        @Test
        @DisplayName("메타데이터를 업데이트한 새로운 Book 인스턴스를 반환한다")
        void updateMetadata() {
            // given
            BookId bookId = BookId.of(1L);
            BookMetadata originalMetadata = createSampleMetadata();
            Book originalBook = Book.of(bookId, originalMetadata);

            BookMetadata newMetadata = BookMetadata.of(
                    "Updated Title",
                    List.of("Updated Author"),
                    "1234567890",
                    "1234567890123",
                    "https://example.com/new-cover.jpg",
                    "New Publisher",
                    "2024-01-01",
                    "Updated description",
                    "ko",
                    500,
                    "new-vol-123"
            );

            // when
            Book updatedBook = originalBook.updateMetadata(newMetadata);

            // then
            assertThat(updatedBook).isNotNull();
            assertThat(updatedBook).isNotSameAs(originalBook); // 불변성: 새로운 인스턴스
            assertThat(updatedBook.getId()).isEqualTo(bookId); // ID는 유지
            assertThat(updatedBook.getMetadata()).isEqualTo(newMetadata);
            assertThat(originalBook.getMetadata()).isEqualTo(originalMetadata); // 원본은 변경되지 않음
        }

        @Test
        @DisplayName("메타데이터 업데이트 시 원본 Book은 변경되지 않는다 (불변성)")
        void updateMetadataImmutability() {
            // given
            BookId bookId = BookId.of(1L);
            BookMetadata originalMetadata = createSampleMetadata();
            Book originalBook = Book.of(bookId, originalMetadata);

            BookMetadata newMetadata = BookMetadata.of(
                    "New Title",
                    List.of("New Author"),
                    null, null, null, null, null, null, null, null, null
            );

            // when
            Book updatedBook = originalBook.updateMetadata(newMetadata);

            // then
            assertThat(originalBook.getMetadata().getTitle()).isEqualTo("Clean Architecture");
            assertThat(updatedBook.getMetadata().getTitle()).isEqualTo("New Title");
        }
    }

    @Nested
    @DisplayName("Book 동등성")
    class BookEquality {

        @Test
        @DisplayName("같은 ID를 가진 Book은 동등하다")
        void equalBooksWithSameId() {
            // given
            BookId bookId = BookId.of(1L);
            BookMetadata metadata1 = createSampleMetadata();
            BookMetadata metadata2 = BookMetadata.of(
                    "Different Title",
                    List.of("Different Author"),
                    null, null, null, null, null, null, null, null, null
            );

            Book book1 = Book.of(bookId, metadata1);
            Book book2 = Book.of(bookId, metadata2);

            // when & then
            assertThat(book1).isEqualTo(book2); // ID가 같으면 동등
            assertThat(book1.hashCode()).isEqualTo(book2.hashCode());
        }

        @Test
        @DisplayName("다른 ID를 가진 Book은 동등하지 않다")
        void notEqualBooksWithDifferentId() {
            // given
            BookMetadata metadata = createSampleMetadata();
            Book book1 = Book.of(BookId.of(1L), metadata);
            Book book2 = Book.of(BookId.of(2L), metadata);

            // when & then
            assertThat(book1).isNotEqualTo(book2);
        }

        @Test
        @DisplayName("ID가 null인 Book들은 동등하다 (Lombok 기본 동작)")
        void nullIdBooksEquality() {
            // given
            BookMetadata metadata1 = createSampleMetadata();
            BookMetadata metadata2 = BookMetadata.of(
                    "Different Title",
                    List.of("Different Author"),
                    null, null, null, null, null, null, null, null, null
            );

            Book book1 = Book.of(null, metadata1);
            Book book2 = Book.of(null, metadata2);

            // when & then
            // @EqualsAndHashCode(of = "id")는 ID가 모두 null이면 동등하다고 판단
            assertThat(book1).isEqualTo(book2);
            assertThat(book1.hashCode()).isEqualTo(book2.hashCode());
        }

        @Test
        @DisplayName("Book은 자기 자신과 동등하다")
        void equalToItself() {
            // given
            Book book = Book.of(BookId.of(1L), createSampleMetadata());

            // when & then
            assertThat(book).isEqualTo(book);
        }

        @Test
        @DisplayName("Book은 null과 동등하지 않다")
        void notEqualToNull() {
            // given
            Book book = Book.of(BookId.of(1L), createSampleMetadata());

            // when & then
            assertThat(book).isNotEqualTo(null);
        }
    }

    @Nested
    @DisplayName("Book toString")
    class BookToString {

        @Test
        @DisplayName("toString()은 Book 정보를 포함한 문자열을 반환한다")
        void toStringContainsBookInfo() {
            // given
            BookId bookId = BookId.of(1L);
            BookMetadata metadata = createSampleMetadata();
            Book book = Book.of(bookId, metadata);

            // when
            String result = book.toString();

            // then
            assertThat(result).contains("Book");
            assertThat(result).contains("id=");
            assertThat(result).contains("metadata=");
        }
    }

    @Nested
    @DisplayName("Book 불변성")
    class BookImmutability {

        @Test
        @DisplayName("Book은 불변 객체이다 - 메타데이터 변경 시 새 인스턴스 반환")
        void bookIsImmutable() {
            // given
            BookId bookId = BookId.of(1L);
            BookMetadata originalMetadata = createSampleMetadata();
            Book book = Book.of(bookId, originalMetadata);

            // when
            BookMetadata newMetadata = BookMetadata.of(
                    "New Title",
                    List.of("New Author"),
                    null, null, null, null, null, null, null, null, null
            );
            Book updatedBook = book.updateMetadata(newMetadata);

            // then
            assertThat(book).isNotSameAs(updatedBook);
            assertThat(book.getMetadata()).isEqualTo(originalMetadata);
            assertThat(updatedBook.getMetadata()).isEqualTo(newMetadata);
        }
    }
}
