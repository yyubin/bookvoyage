package org.yyubin.infrastructure.recommendation.adapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.infrastructure.persistence.book.BookEntity;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.ReviewJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.search.SearchQueryLogEntity;
import org.yyubin.infrastructure.persistence.search.SearchQueryLogJpaRepository;
import org.yyubin.infrastructure.persistence.userbook.UserBookEntity;
import org.yyubin.infrastructure.persistence.userbook.UserBookJpaRepository;

@Component
@RequiredArgsConstructor
public class UserAnalysisContextAdapter implements UserAnalysisContextPort {

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewKeywordJpaRepository reviewKeywordJpaRepository;
    private final BookJpaRepository bookJpaRepository;
    private final UserBookJpaRepository userBookJpaRepository;
    private final SearchQueryLogJpaRepository searchQueryLogJpaRepository;

    @Override
    public UserAnalysisContext loadContext(
        Long userId,
        int reviewLimit,
        int libraryLimit,
        int searchLimit,
        LocalDateTime searchSince
    ) {
        List<ReviewSnapshot> recentReviews = loadRecentReviews(userId, reviewLimit);
        List<UserBookSnapshot> recentLibraryUpdates = loadRecentLibraryUpdates(userId, libraryLimit);
        List<String> recentSearchQueries = loadRecentSearchQueries(userId, searchSince, searchLimit);

        return new UserAnalysisContext(userId, recentReviews, recentLibraryUpdates, recentSearchQueries);
    }

    private List<ReviewSnapshot> loadRecentReviews(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        List<ReviewEntity> reviews = reviewJpaRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
            userId,
            PageRequest.of(0, limit)
        );

        if (reviews.isEmpty()) {
            return List.of();
        }

        List<Long> reviewIds = reviews.stream()
            .map(ReviewEntity::getId)
            .toList();
        List<Long> bookIds = reviews.stream()
            .map(ReviewEntity::getBookId)
            .distinct()
            .toList();

        Map<Long, List<String>> keywordsByReviewId = reviewKeywordJpaRepository
            .findByIdReviewIdIn(reviewIds)
            .stream()
            .collect(Collectors.groupingBy(
                entity -> entity.getId().getReviewId(),
                Collectors.mapping(entity -> entity.getKeyword().getNormalizedValue(), Collectors.toList())
            ));

        Map<Long, BookEntity> bookMap = bookJpaRepository.findAllById(bookIds).stream()
            .collect(Collectors.toMap(BookEntity::getId, book -> book));

        List<ReviewSnapshot> snapshots = new ArrayList<>();
        for (ReviewEntity review : reviews) {
            BookEntity book = bookMap.get(review.getBookId());
            String title = book != null ? book.getTitle() : "";
            List<String> authors = book != null ? book.toDomain().getMetadata().getAuthors() : List.of();

            snapshots.add(new ReviewSnapshot(
                review.getId(),
                review.getBookId(),
                title,
                authors,
                review.getRating(),
                review.getGenre() != null ? review.getGenre().name() : null,
                review.getSummary(),
                keywordsByReviewId.getOrDefault(review.getId(), List.of()),
                review.getCreatedAt()
            ));
        }

        return snapshots;
    }

    private List<UserBookSnapshot> loadRecentLibraryUpdates(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }

        List<UserBookEntity> userBooks = userBookJpaRepository.findByUserIdAndDeletedFalseOrderByUpdatedAtDesc(
            userId,
            PageRequest.of(0, limit)
        );

        if (userBooks.isEmpty()) {
            return List.of();
        }

        List<Long> bookIds = userBooks.stream()
            .map(UserBookEntity::getBookId)
            .distinct()
            .toList();

        Map<Long, BookEntity> bookMap = bookJpaRepository.findAllById(bookIds).stream()
            .collect(Collectors.toMap(BookEntity::getId, book -> book));

        List<UserBookSnapshot> snapshots = new ArrayList<>();
        for (UserBookEntity userBook : userBooks) {
            BookEntity book = bookMap.get(userBook.getBookId());
            String title = book != null ? book.getTitle() : "";
            List<String> authors = book != null ? book.toDomain().getMetadata().getAuthors() : List.of();

            snapshots.add(new UserBookSnapshot(
                userBook.getBookId(),
                title,
                authors,
                userBook.getStatus() != null ? userBook.getStatus().name() : null,
                userBook.getPersonalRating(),
                userBook.getPersonalMemo(),
                userBook.getUpdatedAt()
            ));
        }

        return snapshots;
    }

    private List<String> loadRecentSearchQueries(Long userId, LocalDateTime since, int limit) {
        if (limit <= 0 || since == null) {
            return List.of();
        }

        List<SearchQueryLogEntity> queries = searchQueryLogJpaRepository
            .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since, PageRequest.of(0, limit));

        if (queries.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (SearchQueryLogEntity query : queries) {
            if (query.getNormalizedQuery() != null && !query.getNormalizedQuery().isBlank()) {
                normalized.add(query.getNormalizedQuery());
            }
        }

        return new ArrayList<>(normalized);
    }
}
