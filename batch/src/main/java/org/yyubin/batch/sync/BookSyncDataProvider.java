package org.yyubin.batch.sync;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.batch.service.BatchBookSyncService;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookMetadata;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.wishlist.WishlistJpaRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookSyncDataProvider implements BatchBookSyncService {

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("yyyy")
    );

    private final ReviewJpaRepository reviewJpaRepository;
    private final WishlistJpaRepository wishlistJpaRepository;
    private final ReviewKeywordJpaRepository reviewKeywordJpaRepository;

    @Override
    public BookSyncDto buildSyncData(BookEntity entity) {
        Book book = entity.toDomain();
        BookMetadata metadata = book.getMetadata();

        List<ReviewEntity> reviews = reviewJpaRepository.findByBookId(entity.getId());
        long reviewCount = reviews.size();
        long viewCount = reviews.stream()
                .map(ReviewEntity::getViewCount)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        long wishlistCount = wishlistJpaRepository.countByBookId(entity.getId());
        Double averageRating = reviewJpaRepository.calculateAverageRating(entity.getId());

        Set<String> genres = reviews.stream()
                .map(ReviewEntity::getGenre)
                .filter(Objects::nonNull)
                .map(Enum::name)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        Set<String> topics = resolveTopics(reviews);

        return new BookSyncDto(
                entity.getId(),
                metadata.getTitle(),
                resolveIsbn(metadata),
                metadata.getDescription(),
                parsePublishedDate(metadata.getPublishedDate()),
                metadata.getAuthors(),
                List.copyOf(genres),
                List.copyOf(topics),
                toInt(viewCount),
                toInt(wishlistCount),
                toInt(reviewCount),
                averageRating != null ? averageRating.floatValue() : null
        );
    }

    private Set<String> resolveTopics(List<ReviewEntity> reviews) {
        List<Long> reviewIds = reviews.stream()
                .map(ReviewEntity::getId)
                .filter(Objects::nonNull)
                .toList();

        if (reviewIds.isEmpty()) {
            return Set.of();
        }

        List<ReviewKeywordEntity> keywordMappings = reviewKeywordJpaRepository.findByIdReviewIdIn(reviewIds);
        return keywordMappings.stream()
                .map(ReviewKeywordEntity::getKeyword)
                .filter(Objects::nonNull)
                .map(kw -> kw.getNormalizedValue() != null ? kw.getNormalizedValue() : kw.getRawValue())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private LocalDate parsePublishedDate(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(rawDate, formatter);
            } catch (DateTimeParseException ignored) {
                // try next
            }
        }
        log.debug("Could not parse publishedDate '{}'", rawDate);
        return null;
    }

    private String resolveIsbn(BookMetadata metadata) {
        if (metadata.getIsbn13() != null && !metadata.getIsbn13().isBlank()) {
            return metadata.getIsbn13();
        }
        if (metadata.getIsbn10() != null && !metadata.getIsbn10().isBlank()) {
            return metadata.getIsbn10();
        }
        return null;
    }

    private int toInt(long value) {
        if (value > Integer.MAX_VALUE) {
            log.warn("Value {} exceeds Integer.MAX_VALUE, capping", value);
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }
}
