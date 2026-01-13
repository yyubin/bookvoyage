package org.yyubin.domain.book;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class BookSearchItem {

    private final String title;
    private final List<String> authors;
    private final String isbn10;
    private final String isbn13;
    private final String coverUrl;
    private final String publisher;
    private final String publishedDate;
    private final String description;
    private final String language;
    private final Integer pageCount;
    private final String googleVolumeId;

    // 웹소설 관련 필드
    private final BookType bookType;
    private final WebNovelPlatform platform;
    private final String platformUrl;

    private BookSearchItem(
            String title,
            List<String> authors,
            String isbn10,
            String isbn13,
            String coverUrl,
            String publisher,
            String publishedDate,
            String description,
            String language,
            Integer pageCount,
            String googleVolumeId,
            BookType bookType,
            WebNovelPlatform platform,
            String platformUrl
    ) {
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.authors = authors == null ? Collections.emptyList() : List.copyOf(authors);
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
        this.coverUrl = coverUrl;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.description = description;
        this.language = language;
        this.pageCount = pageCount;
        this.googleVolumeId = googleVolumeId;
        this.bookType = bookType != null ? bookType : BookType.PUBLISHED_BOOK;
        this.platform = platform;
        this.platformUrl = platformUrl;
    }

    /**
     * 기존 출판 도서용 팩토리 메서드 (하위 호환성)
     */
    public static BookSearchItem of(
            String title,
            List<String> authors,
            String isbn10,
            String isbn13,
            String coverUrl,
            String publisher,
            String publishedDate,
            String description,
            String language,
            Integer pageCount,
            String googleVolumeId
    ) {
        return new BookSearchItem(
                title,
                authors,
                isbn10,
                isbn13,
                coverUrl,
                publisher,
                publishedDate,
                description,
                language,
                pageCount,
                googleVolumeId,
                BookType.PUBLISHED_BOOK,
                null,
                null
        );
    }

    /**
     * 웹소설 지원을 포함한 완전한 팩토리 메서드
     */
    public static BookSearchItem of(
            String title,
            List<String> authors,
            String isbn10,
            String isbn13,
            String coverUrl,
            String publisher,
            String publishedDate,
            String description,
            String language,
            Integer pageCount,
            String googleVolumeId,
            BookType bookType,
            WebNovelPlatform platform,
            String platformUrl
    ) {
        return new BookSearchItem(
                title,
                authors,
                isbn10,
                isbn13,
                coverUrl,
                publisher,
                publishedDate,
                description,
                language,
                pageCount,
                googleVolumeId,
                bookType,
                platform,
                platformUrl
        );
    }
}
