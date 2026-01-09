package org.yyubin.application.book.port;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.yyubin.application.book.dto.ShelfAdditionTrendResult;

public interface ShelfAdditionCachePort {
    Optional<ShelfAdditionTrendResult> get(LocalDate date, ZoneId timezone, int limit);

    void put(LocalDate date, ZoneId timezone, int limit, ShelfAdditionTrendResult result);
}
