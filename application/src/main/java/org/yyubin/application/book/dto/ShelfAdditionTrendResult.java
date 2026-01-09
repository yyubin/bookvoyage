package org.yyubin.application.book.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ShelfAdditionTrendResult(
        LocalDate date,
        String timezone,
        int limit,
        List<ShelfAdditionTrendItem> items,
        boolean cacheHit,
        LocalDateTime generatedAt
) {
}
