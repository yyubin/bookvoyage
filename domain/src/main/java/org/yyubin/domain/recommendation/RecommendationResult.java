package org.yyubin.domain.recommendation;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.yyubin.domain.book.BookId;

import java.util.Objects;

/**
 * Value Object representing a book recommendation result with similarity score
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RecommendationResult {
    private final BookId bookId;
    private final SimilarityScore score;
    private final String reason;

    public static RecommendationResult of(BookId bookId, SimilarityScore score, String reason) {
        Objects.requireNonNull(bookId, "Book ID cannot be null");
        Objects.requireNonNull(score, "Similarity score cannot be null");

        return new RecommendationResult(bookId, score, reason != null ? reason : "");
    }
}
