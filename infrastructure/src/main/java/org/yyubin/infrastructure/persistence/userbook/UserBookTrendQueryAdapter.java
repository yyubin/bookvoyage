package org.yyubin.infrastructure.persistence.userbook;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.yyubin.application.book.port.ShelfAdditionTrendPort;
import org.yyubin.application.book.port.dto.ShelfAdditionCount;

@Component
@RequiredArgsConstructor
public class UserBookTrendQueryAdapter implements ShelfAdditionTrendPort {

    private final UserBookJpaRepository userBookJpaRepository;

    @Override
    public List<ShelfAdditionCount> findTopAdditions(LocalDateTime start, LocalDateTime end, int limit) {
        List<ShelfAdditionCountRow> rows = userBookJpaRepository.findTopShelfAdditions(
                start,
                end,
                PageRequest.of(0, limit)
        );

        return rows.stream()
                .map(row -> new ShelfAdditionCount(row.bookId(), row.addedCount()))
                .collect(Collectors.toList());
    }
}
