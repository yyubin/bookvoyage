package org.yyubin.infrastructure.external.book;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.search.dto.ExternalBookSearchResult;
import org.yyubin.application.book.search.port.ExternalBookSearchPort;
import org.yyubin.application.book.search.query.SearchBooksQuery;
import org.yyubin.domain.book.BookSearchItem;
import org.yyubin.infrastructure.external.book.dto.GoogleBookImageLinksResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBookIndustryIdentifierResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBookItemResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBookVolumeInfoResponse;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksSearchRequest;
import org.yyubin.infrastructure.external.book.dto.GoogleBooksVolumeResponse;

@Component
@RequiredArgsConstructor
public class GoogleBooksSearchAdapter implements ExternalBookSearchPort {

    private final GoogleBooksClient client;
    private final GoogleBooksProperties properties;

    @Override
    public ExternalBookSearchResult search(SearchBooksQuery query) {
        GoogleBooksSearchRequest request = new GoogleBooksSearchRequest(
                query.keyword(),
                query.startIndex(),
                Math.min(query.size(), properties.getMaxResults()),
                query.language(),
                query.orderBy() != null ? query.orderBy().externalValue() : null,
                query.printType() != null ? query.printType().externalValue() : null
        );

        GoogleBooksVolumeResponse response = client.search(request);
        List<BookSearchItem> items = mapItems(response);
        int totalItems = response != null && response.totalItems() != null ? response.totalItems() : 0;

        return new ExternalBookSearchResult(items, totalItems);
    }

    private List<BookSearchItem> mapItems(GoogleBooksVolumeResponse response) {
        if (response == null || response.items() == null) {
            return Collections.emptyList();
        }

        return response.items().stream()
                .map(GoogleBookItemResponse::volumeInfo)
                .filter(Objects::nonNull)
                .filter(info -> info.title() != null && !info.title().isBlank())
                .map(this::toDomain)
                .toList();
    }

    private BookSearchItem toDomain(GoogleBookVolumeInfoResponse info) {
        return BookSearchItem.of(
                info.title(),
                info.authors(),
                extractIsbn(info, "ISBN_10"),
                extractIsbn(info, "ISBN_13"),
                resolveCoverUrl(info.imageLinks()),
                info.publisher(),
                info.publishedDate(),
                info.description(),
                info.language(),
                info.pageCount()
        );
    }

    private String extractIsbn(GoogleBookVolumeInfoResponse info, String targetType) {
        if (info.industryIdentifiers() == null) {
            return null;
        }
        return info.industryIdentifiers().stream()
                .filter(identifier -> targetType.equalsIgnoreCase(identifier.type()))
                .map(GoogleBookIndustryIdentifierResponse::identifier)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String resolveCoverUrl(GoogleBookImageLinksResponse imageLinks) {
        if (imageLinks == null) {
            return null;
        }
        return Optional.ofNullable(imageLinks.thumbnail())
                .orElse(imageLinks.smallThumbnail());
    }
}
