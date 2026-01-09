package org.yyubin.application.book.port;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.application.book.port.dto.ShelfAdditionCount;

public interface ShelfAdditionTrendPort {
    List<ShelfAdditionCount> findTopAdditions(LocalDateTime start, LocalDateTime end, int limit);
}
