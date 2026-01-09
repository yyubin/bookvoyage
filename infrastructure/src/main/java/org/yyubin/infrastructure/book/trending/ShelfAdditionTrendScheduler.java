package org.yyubin.infrastructure.book.trending;

import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.GetShelfAdditionTrendUseCase;
import org.yyubin.application.book.query.GetShelfAdditionTrendQuery;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShelfAdditionTrendScheduler {

    private final GetShelfAdditionTrendUseCase getShelfAdditionTrendUseCase;

    @Value("${book.trending.shelf-additions.enabled:true}")
    private boolean enabled;

    @Value("${book.trending.shelf-additions.timezone:Asia/Seoul}")
    private String timezone;

    @Value("${book.trending.shelf-additions.default-limit:20}")
    private int defaultLimit;

    @Scheduled(fixedDelayString = "${book.trending.shelf-additions.refresh-interval-ms:600000}")
    public void refreshTodayCache() {
        if (!enabled) {
            return;
        }

        ZoneId zoneId = ZoneId.of(timezone);
        LocalDate today = LocalDate.now(zoneId);
        GetShelfAdditionTrendQuery query = new GetShelfAdditionTrendQuery(
                today,
                zoneId,
                defaultLimit,
                true
        );

        try {
            getShelfAdditionTrendUseCase.query(query);
        } catch (Exception e) {
            log.warn("Failed to refresh shelf addition trend cache", e);
        }
    }
}
