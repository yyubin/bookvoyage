package org.yyubin.application.book.query;

import java.time.LocalDate;
import java.time.ZoneId;

public record GetShelfAdditionTrendQuery(
        LocalDate date,
        ZoneId timezone,
        int limit,
        boolean forceRefresh
) {
}
