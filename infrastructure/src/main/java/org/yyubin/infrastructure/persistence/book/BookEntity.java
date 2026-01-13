package org.yyubin.infrastructure.persistence.book;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.yyubin.domain.book.BookType;
import org.yyubin.domain.book.WebNovelPlatform;

@Entity
@Table(name = "book")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BookEntity {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "authors", columnDefinition = "TEXT", nullable = false)
    private String authors;

    @Column(name = "isbn_10", length = 50)
    private String isbn10;

    @Column(name = "isbn_13", length = 50)
    private String isbn13;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "publisher", length = 200)
    private String publisher;

    @Column(name = "published_date", length = 50)
    private String publishedDate;

    @Column(name = "language", length = 20)
    private String language;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "google_volume_id", length = 100)
    private String googleVolumeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", nullable = false, length = 30)
    private BookType bookType;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", length = 30)
    private WebNovelPlatform platform;

    @Column(name = "platform_url", length = 500)
    private String platformUrl;

    public static BookEntity fromDomain(Book book) {
        return BookEntity.builder()
                .id(book.getId() != null ? book.getId().getValue() : null)
                .title(book.getMetadata().getTitle())
                .authors(writeAuthors(book.getMetadata().getAuthors()))
                .isbn10(book.getMetadata().getIsbn10())
                .isbn13(book.getMetadata().getIsbn13())
                .coverUrl(book.getMetadata().getCoverUrl())
                .description(book.getMetadata().getDescription())
                .publisher(book.getMetadata().getPublisher())
                .publishedDate(book.getMetadata().getPublishedDate())
                .language(book.getMetadata().getLanguage())
                .pageCount(book.getMetadata().getPageCount())
                .googleVolumeId(book.getMetadata().getGoogleVolumeId())
                .bookType(book.getMetadata().getBookType() != null ? book.getMetadata().getBookType() : BookType.PUBLISHED_BOOK)
                .platform(book.getMetadata().getPlatform())
                .platformUrl(book.getMetadata().getPlatformUrl())
                .build();
    }

    public Book toDomain() {
        return Book.of(
                BookId.of(this.id),
                BookMetadata.of(
                        this.title,
                        readAuthors(this.authors),
                        this.isbn10,
                        this.isbn13,
                        this.coverUrl,
                        this.publisher,
                        this.publishedDate,
                        this.description,
                        this.language,
                        this.pageCount,
                        this.googleVolumeId,
                        this.bookType != null ? this.bookType : BookType.PUBLISHED_BOOK,
                        this.platform,
                        this.platformUrl
                )
        );
    }

    private static String writeAuthors(java.util.List<String> authors) {
        try {
            return OBJECT_MAPPER.writeValueAsString(authors);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize authors", e);
        }
    }

    private static java.util.List<String> readAuthors(String authorsJson) {
        if (authorsJson == null || authorsJson.isBlank()) {
            return java.util.Collections.singletonList("Unknown");
        }
        try {
            return OBJECT_MAPPER.readValue(authorsJson, new TypeReference<java.util.List<String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize authors", e);
        }
    }
}
