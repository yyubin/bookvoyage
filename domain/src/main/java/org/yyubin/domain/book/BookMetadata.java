package org.yyubin.domain.book;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Value Object for Book metadata
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BookMetadata {
    private final String title;
    private final List<String> authors;
    private final String isbn10;
    private final String isbn13;
    private final String coverUrl;
    private final String publisher;
    private final String publishedDate;
    private final String language;
    private final Integer pageCount;
    private final String description;
    private final String googleVolumeId;

    // 웹소설 관련 필드
    private final BookType bookType;
    private final WebNovelPlatform platform;
    private final String platformUrl;

    /**
     * 기존 출판 도서용 팩토리 메서드 (하위 호환성)
     */
    public static BookMetadata of(
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
        return of(
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
    public static BookMetadata of(
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
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        List<String> normalizedAuthors = normalizeAuthors(authors);
        BookType finalBookType = bookType != null ? bookType : BookType.PUBLISHED_BOOK;

        // 웹소설인 경우 플랫폼 URL 검증
        if (finalBookType == BookType.WEB_NOVEL && (platformUrl == null || platformUrl.isBlank())) {
            throw new IllegalArgumentException("Web novel must have platform URL");
        }

        return new BookMetadata(
                title,
                normalizedAuthors,
                isbn10,
                isbn13,
                coverUrl,
                publisher,
                publishedDate,
                language,
                pageCount,
                description != null ? description : "",
                googleVolumeId,
                finalBookType,
                platform,
                platformUrl
        );
    }

    private static List<String> normalizeAuthors(List<String> authors) {
        if (authors == null || authors.isEmpty()) {
            throw new IllegalArgumentException("Book authors cannot be empty");
        }
        List<String> cleaned = authors.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Book authors cannot be empty");
        }
        return Collections.unmodifiableList(cleaned);
    }
}
