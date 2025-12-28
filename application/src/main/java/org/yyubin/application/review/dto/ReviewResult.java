package org.yyubin.application.review.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewVisibility;

public record ReviewResult(
        Long reviewId,
        Long userId,
        String authorNickname,
        String authorTasteTag,
        Long bookId,
        String title,
        List<String> authors,
        String isbn10,
        String isbn13,
        String coverUrl,
        String publisher,
        String publishedDate,
        String description,
        String language,
        Integer pageCount,
        String googleVolumeId,
        int rating,
        String summary,
        String content,
        LocalDateTime createdAt,
        ReviewVisibility visibility,
        boolean deleted,
        long viewCount,
        BookGenre genre,
        List<String> keywords,
        List<String> highlights,
        List<org.yyubin.domain.review.Mention> mentions,
        boolean bookmarked,
        List<ReactionSummary> reactions,
        String userReaction,
        boolean isLiked,
        long likeCount
) {

    public record ReactionSummary(String emoji, long count) {
    }

    public static ReviewResult from(Review review, Book book, org.yyubin.domain.user.User author, List<String> keywords, List<String> highlights) {
        return new ReviewResult(
                review.getId().getValue(),
                author.id().value(),
                author.nickname(),
                author.tasteTag(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthors(),
                book.getMetadata().getIsbn10(),
                book.getMetadata().getIsbn13(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getPublisher(),
                book.getMetadata().getPublishedDate(),
                book.getMetadata().getDescription(),
                book.getMetadata().getLanguage(),
                book.getMetadata().getPageCount(),
                book.getMetadata().getGoogleVolumeId(),
                review.getRating().getValue(),
                review.getSummary(),
                review.getContent(),
                review.getCreatedAt(),
                review.getVisibility(),
                review.isDeleted(),
                review.getViewCount(),
                review.getGenre(),
                keywords != null ? keywords : Collections.emptyList(),
                highlights != null ? highlights : Collections.emptyList(),
                review.getMentions(),
                false,
                Collections.emptyList(),
                null,
                false,
                0L
        );
    }

    public static ReviewResult fromWithViewCount(Review review, Book book, org.yyubin.domain.user.User author, List<String> keywords, List<String> highlights, long viewCount) {
        return new ReviewResult(
                review.getId().getValue(),
                author.id().value(),
                author.nickname(),
                author.tasteTag(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthors(),
                book.getMetadata().getIsbn10(),
                book.getMetadata().getIsbn13(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getPublisher(),
                book.getMetadata().getPublishedDate(),
                book.getMetadata().getDescription(),
                book.getMetadata().getLanguage(),
                book.getMetadata().getPageCount(),
                book.getMetadata().getGoogleVolumeId(),
                review.getRating().getValue(),
                review.getSummary(),
                review.getContent(),
                review.getCreatedAt(),
                review.getVisibility(),
                review.isDeleted(),
                viewCount,
                review.getGenre(),
                keywords != null ? keywords : Collections.emptyList(),
                highlights != null ? highlights : Collections.emptyList(),
                review.getMentions(),
                false,
                Collections.emptyList(),
                null,
                false,
                0L
        );
    }

    public static ReviewResult fromWithViewCountAndInteraction(
            Review review,
            Book book,
            org.yyubin.domain.user.User author,
            List<String> keywords,
            List<String> highlights,
            long viewCount,
            boolean bookmarked,
            List<ReactionSummary> reactions,
            String userReaction,
            boolean isLiked,
            long likeCount
    ) {
        return new ReviewResult(
                review.getId().getValue(),
                author.id().value(),
                author.nickname(),
                author.tasteTag(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                book.getMetadata().getAuthors(),
                book.getMetadata().getIsbn10(),
                book.getMetadata().getIsbn13(),
                book.getMetadata().getCoverUrl(),
                book.getMetadata().getPublisher(),
                book.getMetadata().getPublishedDate(),
                book.getMetadata().getDescription(),
                book.getMetadata().getLanguage(),
                book.getMetadata().getPageCount(),
                book.getMetadata().getGoogleVolumeId(),
                review.getRating().getValue(),
                review.getSummary(),
                review.getContent(),
                review.getCreatedAt(),
                review.getVisibility(),
                review.isDeleted(),
                viewCount,
                review.getGenre(),
                keywords != null ? keywords : Collections.emptyList(),
                highlights != null ? highlights : Collections.emptyList(),
                review.getMentions(),
                bookmarked,
                reactions != null ? reactions : Collections.emptyList(),
                userReaction,
                isLiked,
                likeCount
        );
    }
}
