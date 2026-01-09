package org.yyubin.application.book.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.book.GetShelfAdditionTrendUseCase;
import org.yyubin.application.book.dto.ShelfAdditionTrendBook;
import org.yyubin.application.book.dto.ShelfAdditionTrendItem;
import org.yyubin.application.book.dto.ShelfAdditionTrendResult;
import org.yyubin.application.book.port.ShelfAdditionCachePort;
import org.yyubin.application.book.port.ShelfAdditionTrendPort;
import org.yyubin.application.book.port.dto.ShelfAdditionCount;
import org.yyubin.application.book.query.GetShelfAdditionTrendQuery;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.domain.book.Book;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookTrendService implements GetShelfAdditionTrendUseCase {

    private final ShelfAdditionTrendPort shelfAdditionTrendPort;
    private final ShelfAdditionCachePort shelfAdditionCachePort;
    private final LoadBookPort loadBookPort;

    @Override
    public ShelfAdditionTrendResult query(GetShelfAdditionTrendQuery query) {
        LocalDate date = query.date();
        ZoneId timezone = query.timezone();
        int limit = query.limit();

        if (!query.forceRefresh()) {
            return shelfAdditionCachePort.get(date, timezone, limit)
                    .map(result -> withCacheHit(result, true))
                    .orElseGet(() -> fetchAndCache(date, timezone, limit));
        }

        return fetchAndCache(date, timezone, limit);
    }

    private ShelfAdditionTrendResult fetchAndCache(LocalDate date, ZoneId timezone, int limit) {
        LocalDateTime start = ZonedDateTime.of(date, java.time.LocalTime.MIN, timezone).toLocalDateTime();
        LocalDateTime end = ZonedDateTime.of(date.plusDays(1), java.time.LocalTime.MIN, timezone).toLocalDateTime();

        List<ShelfAdditionCount> counts = shelfAdditionTrendPort.findTopAdditions(start, end, limit);
        List<ShelfAdditionTrendItem> items = new ArrayList<>();
        int rank = 1;

        for (ShelfAdditionCount count : counts) {
            Book book = loadBookPort.loadById(count.bookId()).orElse(null);
            if (book == null || book.getMetadata() == null) {
                continue;
            }
            ShelfAdditionTrendBook summary = new ShelfAdditionTrendBook(
                    book.getId().getValue(),
                    book.getMetadata().getTitle(),
                    book.getMetadata().getAuthors(),
                    book.getMetadata().getCoverUrl()
            );
            items.add(new ShelfAdditionTrendItem(rank++, summary, count.addedCount()));
        }

        ShelfAdditionTrendResult result = new ShelfAdditionTrendResult(
                date,
                timezone.getId(),
                limit,
                items,
                false,
                LocalDateTime.now(timezone)
        );

        shelfAdditionCachePort.put(date, timezone, limit, result);
        return result;
    }

    private ShelfAdditionTrendResult withCacheHit(ShelfAdditionTrendResult result, boolean cacheHit) {
        return new ShelfAdditionTrendResult(
                result.date(),
                result.timezone(),
                result.limit(),
                result.items(),
                cacheHit,
                result.generatedAt()
        );
    }
}
