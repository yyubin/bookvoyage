package org.yyubin.api.book.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.book.dto.ShelfAdditionTrendResult;

public record ShelfAdditionTrendResponse(
        LocalDate date,
        String timezone,
        int limit,
        boolean cacheHit,
        LocalDateTime generatedAt,
        List<ShelfAdditionTrendItemResponse> items
) {
    public record ShelfAdditionTrendItemResponse(
            int rank,
            long addedCount,
            ShelfAdditionTrendBookResponse book
    ) {
    }

    public record ShelfAdditionTrendBookResponse(
            Long bookId,
            String title,
            List<String> authors,
            String coverUrl
    ) {
    }

    public static ShelfAdditionTrendResponse from(ShelfAdditionTrendResult result) {
        List<ShelfAdditionTrendItemResponse> items = result.items().stream()
                .map(item -> new ShelfAdditionTrendItemResponse(
                        item.rank(),
                        item.addedCount(),
                        new ShelfAdditionTrendBookResponse(
                                item.book().bookId(),
                                item.book().title(),
                                item.book().authors(),
                                item.book().coverUrl()
                        )
                ))
                .toList();

        return new ShelfAdditionTrendResponse(
                result.date(),
                result.timezone(),
                result.limit(),
                result.cacheHit(),
                result.generatedAt(),
                items
        );
    }
}
