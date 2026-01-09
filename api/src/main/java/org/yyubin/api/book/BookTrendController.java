package org.yyubin.api.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.yyubin.api.book.dto.ShelfAdditionTrendResponse;
import org.yyubin.application.book.GetShelfAdditionTrendUseCase;
import org.yyubin.application.book.dto.ShelfAdditionTrendResult;
import org.yyubin.application.book.query.GetShelfAdditionTrendQuery;

@RestController
@RequestMapping("/api/books/trending")
@RequiredArgsConstructor
public class BookTrendController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;
    private static final String DEFAULT_TIMEZONE = "Asia/Seoul";

    private final GetShelfAdditionTrendUseCase getShelfAdditionTrendUseCase;

    @GetMapping("/shelf-additions")
    public ResponseEntity<ShelfAdditionTrendResponse> getShelfAdditionsTrend(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "tz", required = false, defaultValue = DEFAULT_TIMEZONE) String timezone,
            @RequestParam(value = "limit", required = false) @Min(1) @Max(MAX_LIMIT) Integer limit,
            @RequestParam(value = "forceRefresh", required = false, defaultValue = "false") boolean forceRefresh
    ) {
        ZoneId zoneId = ZoneId.of(timezone);
        LocalDate requestDate = date == null ? LocalDate.now(zoneId) : date;
        int requestLimit = limit == null ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        ShelfAdditionTrendResult result = getShelfAdditionTrendUseCase.query(
                new GetShelfAdditionTrendQuery(
                        requestDate,
                        zoneId,
                        requestLimit,
                        forceRefresh
                )
        );

        return ResponseEntity.ok(ShelfAdditionTrendResponse.from(result));
    }
}
