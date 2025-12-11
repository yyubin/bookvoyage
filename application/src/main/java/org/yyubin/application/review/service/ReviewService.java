package org.yyubin.application.review.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.CreateReviewUseCase;
import org.yyubin.application.review.DeleteReviewUseCase;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.UpdateReviewUseCase;
import org.yyubin.application.review.command.CreateReviewCommand;
import org.yyubin.application.review.command.DeleteReviewCommand;
import org.yyubin.application.review.command.UpdateReviewCommand;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.SaveBookPort;
import org.yyubin.application.review.port.SaveReviewPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.book.Book;
import org.yyubin.domain.book.BookId;
import org.yyubin.domain.review.Rating;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.user.UserId;
import org.yyubin.domain.review.MentionParser;
import org.yyubin.domain.review.Mention;
import java.util.List;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.application.notification.NotificationMessages;

@Service
@RequiredArgsConstructor
public class ReviewService implements CreateReviewUseCase, UpdateReviewUseCase, DeleteReviewUseCase {

    private final LoadBookPort loadBookPort;
    private final SaveBookPort saveBookPort;
    private final SaveReviewPort saveReviewPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadUserPort loadUserPort;
    private final RegisterKeywordsService registerKeywordsService;
    private final MentionParser mentionParser;
    private final NotificationEventUseCase notificationEventUseCase;
    private final FollowQueryPort followQueryPort;

    @Override
    @Transactional
    public ReviewResult execute(CreateReviewCommand command) {
        UserId userId = new UserId(command.userId());
        loadUserPort.loadById(userId);

        Book book = resolveBook(command);
        List<Mention> mentions = mentionParser.parse(command.content());

        Review review = Review.create(
                userId,
                BookId.of(book.getId().getValue()),
                Rating.of(command.rating()),
                command.content(),
                command.visibility() != null ? command.visibility() : ReviewVisibility.PUBLIC,
                command.genre(),
                mentions
        );

        Review savedReview = saveReviewPort.save(review);
        registerKeywordsService.register(savedReview.getId(), command.keywords());
        notifyFollowersOnNewReview(savedReview);
        notifyMentions(savedReview.getMentions(), userId, savedReview.getId().getValue(), null);
        return ReviewResult.from(savedReview, book, registerKeywordsService.loadKeywords(savedReview.getId()));
    }

    @Override
    @Transactional
    public ReviewResult execute(UpdateReviewCommand command) {
        UserId userId = new UserId(command.userId());
        Review existing = loadReviewPort.loadById(command.reviewId());

        if (!existing.isWrittenBy(userId)) {
            throw new IllegalArgumentException("User is not the author of this review");
        }
        if (existing.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }

        Review updated = existing;

        if (command.rating() != null) {
            updated = updated.updateRating(Rating.of(command.rating()));
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

        Review saved = saveReviewPort.save(updated);
        Book book = loadBookPort.loadById(saved.getBookId().getValue())
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + saved.getBookId().getValue()));
        registerKeywordsService.register(saved.getId(), command.keywords());

        return ReviewResult.from(saved, book, registerKeywordsService.loadKeywords(saved.getId()));
    }

    @Override
    @Transactional
    public ReviewResult execute(DeleteReviewCommand command) {
        UserId userId = new UserId(command.userId());
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

        return ReviewResult.from(saved, book, registerKeywordsService.loadKeywords(saved.getId()));
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

    private Book resolveBook(CreateReviewCommand command) {
        Optional<Book> existingBook = Optional.empty();

        if (command.isbn() != null && !command.isbn().isBlank()) {
            existingBook = loadBookPort.loadByIsbn(command.isbn());
        }

        return existingBook.orElseGet(() -> saveBookPort.save(
                Book.create(
                        command.title(),
                        command.author(),
                        command.isbn(),
                        command.coverUrl(),
                        command.description()
                )
        ));
    }
}
