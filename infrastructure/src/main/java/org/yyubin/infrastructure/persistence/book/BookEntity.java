package org.yyubin.infrastructure.persistence.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.book.BookMetadata;

@Entity
@Table(name = "book")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "author", nullable = false, length = 200)
    private String author;

    @Column(name = "isbn", length = 50)
    private String isbn;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public static BookEntity fromDomain(Book book) {
        return BookEntity.builder()
                .id(book.getId() != null ? book.getId().getValue() : null)
                .title(book.getMetadata().getTitle())
                .author(book.getMetadata().getAuthor())
                .isbn(book.getMetadata().getIsbn())
                .coverUrl(book.getMetadata().getCoverUrl())
                .description(book.getMetadata().getDescription())
                .build();
    }

    public Book toDomain() {
        return Book.of(
                BookId.of(this.id),
                BookMetadata.of(
                        this.title,
                        this.author,
                        this.isbn,
                        this.coverUrl,
                        this.description
                )
        );
    }
}
