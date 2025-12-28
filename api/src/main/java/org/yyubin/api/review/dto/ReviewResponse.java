package org.yyubin.api.review.dto;

import java.time.LocalDateTime;
import java.util.List;
import org.yyubin.api.common.CountFormatter;
import org.yyubin.api.common.TimeFormatter;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.domain.review.BookGenre;
import org.yyubin.domain.review.ReviewVisibility;

public record ReviewResponse(
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
        String createdAt,
        ReviewVisibility visibility,
        boolean deleted,
        String viewCount,
        String genre,
        List<String> keywords,
        List<String> highlights,
        List<MentionResponse> mentions,
        boolean bookmarked,
        List<ReactionSummaryResponse> reactions,
        String userReaction
) {

    public record ReactionSummaryResponse(String emoji, long count) {
    }

    public static ReviewResponse from(ReviewResult result) {
        return new ReviewResponse(
                result.reviewId(),
                result.userId(),
                result.authorNickname(),
                result.authorTasteTag(),
                result.bookId(),
                result.title(),
                result.authors(),
                result.isbn10(),
                result.isbn13(),
                result.coverUrl(),
                result.publisher(),
                result.publishedDate(),
                result.description(),
                result.language(),
                result.pageCount(),
                result.googleVolumeId(),
                result.rating(),
                result.summary(),
                result.content(),
                TimeFormatter.formatRelativeTime(result.createdAt()),
                result.visibility(),
                result.deleted(),
                CountFormatter.format(result.viewCount()),
                result.genre() != null ? result.genre().displayName() : null,
                result.keywords(),
                result.highlights(),
                result.mentions().stream().map(MentionResponse::from).toList(),
                result.bookmarked(),
                result.reactions().stream()
                        .map(r -> new ReactionSummaryResponse(r.emoji(), r.count()))
                        .toList(),
                result.userReaction()
        );
    }
}
