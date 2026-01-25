package org.yyubin.infrastructure.external.book.dto;

import java.util.List;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.domain.book.BookType;
import org.yyubin.domain.book.WebNovelPlatform;

/**
 * Redis 캐싱용 DTO
 * BookSearchItem이 private 생성자만 있어서 역직렬화를 위한 별도 DTO 사용
 */
public record BookSearchCacheDto(
        List<BookSearchItemDto> items,
        int totalItems
) {
    public static BookSearchCacheDto from(ExternalBookSearchResult result) {
        List<BookSearchItemDto> itemDtos = result.items().stream()
                .map(BookSearchItemDto::from)
                .toList();
        return new BookSearchCacheDto(itemDtos, result.totalItems());
    }

    public ExternalBookSearchResult toResult() {
        List<BookSearchItem> domainItems = items.stream()
                .map(BookSearchItemDto::toDomain)
                .toList();
        return new ExternalBookSearchResult(domainItems, totalItems);
    }

    public record BookSearchItemDto(
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
            String bookType,
            String platform,
            String platformUrl
    ) {
        public static BookSearchItemDto from(BookSearchItem item) {
            return new BookSearchItemDto(
                    item.getTitle(),
                    item.getAuthors(),
                    item.getIsbn10(),
                    item.getIsbn13(),
                    item.getCoverUrl(),
                    item.getPublisher(),
                    item.getPublishedDate(),
                    item.getDescription(),
                    item.getLanguage(),
                    item.getPageCount(),
                    item.getGoogleVolumeId(),
                    item.getBookType() != null ? item.getBookType().name() : null,
                    item.getPlatform() != null ? item.getPlatform().name() : null,
                    item.getPlatformUrl()
            );
        }

        public BookSearchItem toDomain() {
            return BookSearchItem.of(
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
                    bookType != null ? BookType.valueOf(bookType) : BookType.PUBLISHED_BOOK,
                    platform != null ? WebNovelPlatform.valueOf(platform) : null,
                    platformUrl
            );
        }
    }
}
