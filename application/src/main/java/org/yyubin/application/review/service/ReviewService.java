package org.yyubin.application.review.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.notification.NotificationMessages;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.review.CreateReviewUseCase;
import org.yyubin.application.review.DeleteReviewUseCase;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.RegisterHighlightsUseCase;
import org.yyubin.application.review.RegisterKeywordsUseCase;
import org.yyubin.application.review.UpdateReviewUseCase;
import org.yyubin.application.review.command.CreateReviewCommand;
import org.yyubin.application.review.command.DeleteReviewCommand;
import org.yyubin.application.review.command.UpdateReviewCommand;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.search.event.ReviewSearchIndexEvent;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventPublisher;
import org.yyubin.application.review.search.event.ReviewSearchIndexEventType;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.application.review.port.SaveReviewPort;
import org.yyubin.application.userbook.EnsureCompletedUserBookUseCase;
import org.yyubin.application.userbook.command.EnsureCompletedUserBookCommand;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.Mention;
import org.yyubin.domain.review.MentionParser;
import org.yyubin.domain.review.Rating;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.notification.NotificationType;

@Service
@RequiredArgsConstructor
public class ReviewService implements CreateReviewUseCase, UpdateReviewUseCase, DeleteReviewUseCase {

    private final LoadBookPort loadBookPort;
    private final SaveBookPort saveBookPort;
    private final SaveReviewPort saveReviewPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadUserPort loadUserPort;
    private final RegisterKeywordsUseCase registerKeywordsUseCase;
    private final LoadKeywordsUseCase loadKeywordsUseCase;
    private final RegisterHighlightsUseCase registerHighlightsUseCase;
    private final LoadHighlightsUseCase loadHighlightsUseCase;
    private final HighlightNormalizer highlightNormalizer;
    private final MentionParser mentionParser;
    private final NotificationEventUseCase notificationEventUseCase;
    private final FollowQueryPort followQueryPort;
    private final EventPublisher eventPublisher;
    private final ReviewSearchIndexEventPublisher reviewSearchIndexEventPublisher;
    private final EnsureCompletedUserBookUseCase ensureCompletedUserBookUseCase;

    @Override
    @Transactional
    public ReviewResult execute(CreateReviewCommand command) {
        UserId userId = new UserId(command.userId());
        org.yyubin.domain.user.User author = loadUserPort.loadById(userId);

        Book book = resolveBook(command);
        List<Mention> mentions = mentionParser.parse(command.content());

        Review review = Review.create(
                userId,
                BookId.of(book.getId().getValue()),
                Rating.of(command.rating()),
                command.summary(),
                command.content(),
                command.visibility() != null ? command.visibility() : ReviewVisibility.PUBLIC,
                command.genre(),
                mentions
        );

        Review savedReview = saveReviewPort.save(review);
        ensureCompletedUserBookUseCase.execute(new EnsureCompletedUserBookCommand(
                userId.value(),
                book.getId().getValue()
        ));
        registerKeywordsUseCase.register(savedReview.getId(), command.keywords());
        registerHighlightsUseCase.register(savedReview.getId(), command.highlights());
        notifyFollowersOnNewReview(savedReview);
        notifyMentions(savedReview.getMentions(), userId, savedReview.getId().getValue(), null);
        publishReviewEvent(
                "REVIEW_CREATED",
                savedReview,
                book,
                loadKeywordsUseCase.loadKeywords(savedReview.getId()),
                loadHighlightsUseCase.loadHighlights(savedReview.getId())
        );
        return ReviewResult.from(
                savedReview,
                book,
                author,
                loadKeywordsUseCase.loadKeywords(savedReview.getId()),
                loadHighlightsUseCase.loadHighlights(savedReview.getId())
        );
    }

    @Override
    @Transactional
    public ReviewResult execute(UpdateReviewCommand command) {
        UserId userId = new UserId(command.userId());
        org.yyubin.domain.user.User author = loadUserPort.loadById(userId);
        Review existing = loadReviewPort.loadById(command.reviewId());

        if (!existing.isWrittenBy(userId)) {
            throw new IllegalArgumentException("User is not the author of this review");
        }
        if (existing.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }

        Review updated = existing;

        Book book = loadBookPort.loadById(existing.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + existing.getBookId().getValue()));

        Book updatedBook = maybeUpdateBookMetadata(book, command);

        if (command.rating() != null) {
            updated = updated.updateRating(Rating.of(command.rating()));
        }
        if (command.summary() != null) {
            updated = updated.updateSummary(command.summary());
        }
        if (command.content() != null) {
            updated = updated.updateContent(command.content(), mentionParser.parse(command.content()));
        }
        if (command.visibility() != null) {
            updated = updated.updateVisibility(command.visibility());
        }
        if (command.genre() != null) {
            updated = updated.updateGenre(command.genre());
        }

        if (updatedBook != book) {
            saveBookPort.save(updatedBook);
        }

        Review saved = saveReviewPort.save(updated);
        registerKeywordsUseCase.register(saved.getId(), command.keywords());
        registerHighlightsUseCase.register(saved.getId(), command.highlights());
        publishReviewEvent(
                "REVIEW_UPDATED",
                saved,
                updatedBook,
                loadKeywordsUseCase.loadKeywords(saved.getId()),
                loadHighlightsUseCase.loadHighlights(saved.getId())
        );

        return ReviewResult.from(
                saved,
                updatedBook,
                author,
                loadKeywordsUseCase.loadKeywords(saved.getId()),
                loadHighlightsUseCase.loadHighlights(saved.getId())
        );
    }

    @Override
    @Transactional
    public ReviewResult execute(DeleteReviewCommand command) {
        UserId userId = new UserId(command.userId());
        org.yyubin.domain.user.User author = loadUserPort.loadById(userId);
        Review existing = loadReviewPort.loadById(command.reviewId());

        if (!existing.isWrittenBy(userId)) {
            throw new IllegalArgumentException("User is not the author of this review");
        }
        if (existing.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }

        Review deleted = existing.markDeleted();
        Review saved = saveReviewPort.save(deleted);

        Book book = loadBookPort.loadById(saved.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + saved.getBookId().getValue()));
        publishReviewEvent(
                "REVIEW_DELETED",
                saved,
                book,
                loadKeywordsUseCase.loadKeywords(saved.getId()),
                loadHighlightsUseCase.loadHighlights(saved.getId())
        );

        return ReviewResult.from(
                saved,
                book,
                author,
                loadKeywordsUseCase.loadKeywords(saved.getId()),
                loadHighlightsUseCase.loadHighlights(saved.getId())
        );
    }

    private void notifyFollowersOnNewReview(Review review) {
        List<Long> followerIds = followQueryPort.loadFollowerIdsAll(review.getUserId().value());
        for (Long followerId : followerIds) {
            if (followerId.equals(review.getUserId().value())) continue;
            notificationEventUseCase.handle(new NotificationEventPayload(
                    followerId,
                    NotificationType.FOLLOWEE_NEW_REVIEW,
                    review.getUserId().value(),
                    review.getId().getValue(),
                    NotificationMessages.FOLLOWEE_NEW_REVIEW
            ));
        }
    }

    private void notifyMentions(List<Mention> mentions, UserId writer, Long reviewId, Long commentId) {
        java.util.Set<Long> unique = new java.util.HashSet<>();
        for (Mention mention : mentions) {
            Long recipient = mention.mentionedUserId();
            if (recipient.equals(writer.value()) || !unique.add(recipient)) {
                continue;
            }
            notificationEventUseCase.handle(new NotificationEventPayload(
                    recipient,
                    NotificationType.MENTION,
                    writer.value(),
                    commentId != null ? commentId : reviewId,
                    NotificationMessages.MENTION
            ));
        }
    }

    private Book maybeUpdateBookMetadata(Book current, UpdateReviewCommand command) {
        boolean hasMetadataUpdate = command.title() != null
                || command.authors() != null
                || command.isbn10() != null
                || command.isbn13() != null
                || command.coverUrl() != null
                || command.publisher() != null
                || command.publishedDate() != null
                || command.description() != null
                || command.language() != null
                || command.pageCount() != null
                || command.googleVolumeId() != null;

        if (!hasMetadataUpdate) {
            return current;
        }

        return current.updateMetadata(
                org.yyubin.domain.book.BookMetadata.of(
                        command.title() != null ? command.title() : current.getMetadata().getTitle(),
                        command.authors() != null ? command.authors() : current.getMetadata().getAuthors(),
                        command.isbn10() != null ? command.isbn10() : current.getMetadata().getIsbn10(),
                        command.isbn13() != null ? command.isbn13() : current.getMetadata().getIsbn13(),
                        command.coverUrl() != null ? command.coverUrl() : current.getMetadata().getCoverUrl(),
                        command.publisher() != null ? command.publisher() : current.getMetadata().getPublisher(),
                        command.publishedDate() != null ? command.publishedDate() : current.getMetadata().getPublishedDate(),
                        command.description() != null ? command.description() : current.getMetadata().getDescription(),
                        command.language() != null ? command.language() : current.getMetadata().getLanguage(),
                        command.pageCount() != null ? command.pageCount() : current.getMetadata().getPageCount(),
                        command.googleVolumeId() != null ? command.googleVolumeId() : current.getMetadata().getGoogleVolumeId()
                )
        );
    }

    private Book resolveBook(CreateReviewCommand command) {
        Optional<Book> existingBook = loadBookPort.loadByIdentifiers(
                command.isbn10(),
                command.isbn13(),
                command.googleVolumeId()
        );

        return existingBook.orElseGet(() -> saveBookPort.save(
                Book.create(
                        command.title(),
                        command.authors(),
                        command.isbn10(),
                        command.isbn13(),
                        command.coverUrl(),
                        command.publisher(),
                        command.publishedDate(),
                        command.description(),
                        command.language(),
                        command.pageCount(),
                        command.googleVolumeId()
                )
        ));
    }

    private void publishReviewEvent(
            String eventType,
            Review review,
            Book book,
            List<String> keywords,
            List<String> highlights
    ) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("bookId", book.getId().getValue());
        metadata.put("bookTitle", book.getMetadata().getTitle());
        metadata.put("rating", review.getRating().getValue());
        metadata.put("visibility", review.getVisibility().name());
        metadata.put("reviewId", review.getId() != null ? review.getId().getValue() : null);
        metadata.put("summary", review.getSummary());
        metadata.put("content", review.getContent());
        metadata.put("genre", review.getGenre().name());
        metadata.put("createdAt", review.getCreatedAt().toString());
        metadata.put("keywords", keywords);
        metadata.put("highlights", highlights);
        metadata.put("highlightsNorm", normalizeHighlights(highlights));
        eventPublisher.publish(
                EventTopics.REVIEW,
                review.getUserId().value().toString(),
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        eventType,
                        review.getUserId().value(),
                        "REVIEW",
                        review.getId() != null ? review.getId().getValue().toString() : null,
                        metadata,
                        java.time.Instant.now(),
                        "api",
                        1
                )
        );

        ReviewSearchIndexEventType indexType = "REVIEW_DELETED".equals(eventType)
                ? ReviewSearchIndexEventType.DELETE
                : ReviewSearchIndexEventType.UPSERT;
        ReviewSearchIndexEvent indexEvent = new ReviewSearchIndexEvent(
                indexType,
                review.getId() != null ? review.getId().getValue() : null,
                review.getUserId().value(),
                book.getId().getValue(),
                book.getMetadata().getTitle(),
                review.getSummary(),
                review.getContent(),
                highlights,
                normalizeHighlights(highlights),
                keywords,
                review.getGenre().name(),
                review.getCreatedAt(),
                review.getRating().getValue()
        );
        reviewSearchIndexEventPublisher.publish(indexEvent);
    }

    private List<String> normalizeHighlights(List<String> highlights) {
        if (highlights == null || highlights.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return highlights.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(highlightNormalizer::normalize)
                .toList();
    }
}
