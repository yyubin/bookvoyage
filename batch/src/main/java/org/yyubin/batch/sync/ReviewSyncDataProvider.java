package org.yyubin.batch.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.batch.service.BatchReviewSyncService;
import org.yyubin.infrastructure.persistence.book.BookJpaRepository;
import org.yyubin.infrastructure.persistence.review.ReviewEntity;
import org.yyubin.infrastructure.persistence.review.highlight.HighlightEntity;
import org.yyubin.infrastructure.persistence.review.highlight.HighlightJpaRepository;
import org.yyubin.infrastructure.persistence.review.highlight.ReviewHighlightEntity;
import org.yyubin.infrastructure.persistence.review.highlight.ReviewHighlightJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.KeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordEntity;
import org.yyubin.infrastructure.persistence.review.keyword.ReviewKeywordJpaRepository;
import org.yyubin.infrastructure.persistence.review.comment.ReviewCommentJpaRepository;
import org.yyubin.infrastructure.persistence.review.reaction.ReviewReactionJpaRepository;
import org.yyubin.infrastructure.persistence.review.bookmark.ReviewBookmarkJpaRepository;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSyncDataProvider implements BatchReviewSyncService {

    private final ReviewReactionJpaRepository reviewReactionJpaRepository;
    private final ReviewBookmarkJpaRepository reviewBookmarkJpaRepository;
    private final ReviewCommentJpaRepository reviewCommentJpaRepository;
    private final ReviewHighlightJpaRepository reviewHighlightJpaRepository;
    private final HighlightJpaRepository highlightJpaRepository;
    private final ReviewKeywordJpaRepository reviewKeywordJpaRepository;
    private final KeywordJpaRepository keywordJpaRepository;
    private final BookJpaRepository bookJpaRepository;

    @Override
    public ReviewSyncDto buildSyncData(ReviewEntity review) {
        int likeCount = safeCount(reviewReactionJpaRepository.countByReviewId(review.getId()));
        int bookmarkCount = safeCount(reviewBookmarkJpaRepository.countByReviewId(review.getId()));
        int commentCount = safeCount(reviewCommentJpaRepository.countByReviewIdAndDeletedFalse(review.getId()));
        long viewCount = review.getViewCount() != null ? review.getViewCount() : 0L;

        String bookTitle = bookJpaRepository.findById(review.getBookId())
                .map(book -> book.getTitle())
                .orElse("");

        HighlightLists highlights = loadHighlights(review.getId());
        List<String> keywords = loadKeywords(review.getId());

        return new ReviewSyncDto(
                review.getId(),
                review.getUserId(),
                review.getBookId(),
                bookTitle,
                review.getSummary(),
                review.getContent(),
                highlights.rawValues(),
                highlights.normalizedValues(),
                keywords,
                review.getRating() != null ? review.getRating().floatValue() : null,
                review.getVisibility() != null ? review.getVisibility().name() : null,
                review.getCreatedAt(),
                likeCount,
                bookmarkCount,
                commentCount,
                viewCount,
                null, // dwellScore placeholder
                review.getGenre() != null ? review.getGenre().name() : null
        );
    }

    private HighlightLists loadHighlights(Long reviewId) {
        List<ReviewHighlightEntity> mappings = reviewHighlightJpaRepository.findByIdReviewId(reviewId);
        if (mappings.isEmpty()) {
            return new HighlightLists(Collections.emptyList(), Collections.emptyList());
        }

        List<Long> highlightIds = mappings.stream()
                .map(mapping -> mapping.getId().getHighlightId())
                .toList();

        Map<Long, HighlightEntity> highlightMap = highlightJpaRepository.findByIdIn(highlightIds).stream()
                .collect(Collectors.toMap(HighlightEntity::getId, Function.identity()));

        List<String> rawValues = highlightIds.stream()
                .map(highlightMap::get)
                .map(entity -> entity != null ? entity.getRawValue() : null)
                .filter(value -> value != null && !value.isBlank())
                .toList();

        List<String> normalizedValues = highlightIds.stream()
                .map(highlightMap::get)
                .map(entity -> entity != null ? entity.getNormalizedValue() : null)
                .filter(value -> value != null && !value.isBlank())
                .toList();

        return new HighlightLists(rawValues, normalizedValues);
    }

    private List<String> loadKeywords(Long reviewId) {
        List<ReviewKeywordEntity> mappings = reviewKeywordJpaRepository.findByIdReviewId(reviewId);
        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> keywordIds = mappings.stream()
                .map(mapping -> mapping.getId().getKeywordId())
                .toList();

        Map<Long, KeywordEntity> keywordMap = keywordJpaRepository.findByIdIn(keywordIds).stream()
                .collect(Collectors.toMap(KeywordEntity::getId, Function.identity()));

        return keywordIds.stream()
                .map(keywordMap::get)
                .map(entity -> entity != null ? entity.getRawValue() : null)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private int safeCount(long value) {
        if (value > Integer.MAX_VALUE) {
            log.warn("Review metric overflow detected, capping at Integer.MAX_VALUE");
            return Integer.MAX_VALUE;
        }
        return (int) value;
    }

    private record HighlightLists(List<String> rawValues, List<String> normalizedValues) {}
}
