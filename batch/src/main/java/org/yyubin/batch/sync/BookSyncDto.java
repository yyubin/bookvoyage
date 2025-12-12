package org.yyubin.batch.sync;

import java.time.LocalDate;
import java.util.List;

public record BookSyncDto(
        Long id,
        String title,
        String isbn,
        String description,
        LocalDate publishedDate,
        List<String> authors,
        List<String> genres,
        List<String> topics,
        int viewCount,
        int wishlistCount,
        int reviewCount,
        Float averageRating
) { }
