package org.yyubin.application.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.review.CreateCommentUseCase;
import org.yyubin.application.review.DeleteCommentUseCase;
import org.yyubin.application.review.GetCommentsUseCase;
import org.yyubin.application.review.dto.PagedCommentResult;
import org.yyubin.application.review.dto.ReviewCommentResult;
import org.yyubin.application.review.UpdateCommentUseCase;
import org.yyubin.application.review.command.CreateCommentCommand;
import org.yyubin.application.review.command.DeleteCommentCommand;
import org.yyubin.application.review.command.UpdateCommentCommand;
import org.yyubin.application.review.port.LoadReviewCommentPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.review.port.SaveReviewCommentPort;
import org.yyubin.application.user.port.LoadUserPort;
import org.yyubin.domain.review.MentionParser;
import org.yyubin.application.notification.NotificationEventUseCase;
import org.yyubin.application.notification.dto.NotificationEventPayload;
import org.yyubin.application.event.EventPayload;
import org.yyubin.application.event.EventPublisher;
import org.yyubin.application.event.EventTopics;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewComment;
import org.yyubin.domain.review.ReviewCommentId;
import org.yyubin.domain.review.ReviewId;
import org.yyubin.domain.notification.NotificationType;
import org.yyubin.domain.user.UserId;
import org.yyubin.application.notification.NotificationMessages;

@Service
@RequiredArgsConstructor
public class ReviewCommentService implements CreateCommentUseCase, UpdateCommentUseCase, DeleteCommentUseCase,
        GetCommentsUseCase, org.yyubin.application.review.GetRepliesUseCase {

    private final LoadReviewPort loadReviewPort;
    private final LoadReviewCommentPort loadReviewCommentPort;
    private final SaveReviewCommentPort saveReviewCommentPort;
    private final LoadUserPort loadUserPort;
    private final MentionParser mentionParser;
    private final NotificationEventUseCase notificationEventUseCase;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public ReviewCommentResult execute(CreateCommentCommand command) {
        UserId writerId = new UserId(command.userId());
        loadUserPort.loadById(writerId);

        Review review = loadReviewPort.loadById(command.reviewId());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }
        if (!review.getVisibility().isPublic() && !review.isWrittenBy(writerId)) {
            throw new IllegalArgumentException("Review not found: " + command.reviewId());
        }
        var mentions = mentionParser.parse(command.content());

        ReviewCommentId parentId = null;
        if (command.parentCommentId() != null) {
            ReviewComment parent = loadReviewCommentPort.loadById(command.parentCommentId());
            if (!parent.getReviewId().equals(ReviewId.of(command.reviewId()))) {
                throw new IllegalArgumentException("Parent comment belongs to a different review");
            }
            if (parent.isDeleted()) {
                throw new IllegalArgumentException("Cannot reply to a deleted comment");
            }
            parentId = parent.getId();
        }

        ReviewComment comment = ReviewComment.create(
                ReviewId.of(command.reviewId()),
                writerId,
                command.content(),
                parentId,
                mentions
        );

        ReviewComment saved = saveReviewCommentPort.save(comment);
        if (!review.isWrittenBy(writerId)) {
            notificationEventUseCase.handle(new NotificationEventPayload(
                    review.getUserId().value(),
                    NotificationType.COMMENT_ON_REVIEW,
                    command.userId(),
                    command.reviewId(),
                    NotificationMessages.COMMENT_ON_REVIEW
            ));
        }
        publishCommentEvent("COMMENT_CREATED", saved, review);
        sendMentionNotifications(mentions, writerId, command.reviewId(), command.parentCommentId());

        org.yyubin.domain.user.User author = loadUserPort.loadById(writerId);
        long replyCount = 0; // 새로 생성된 댓글은 대댓글이 없음
        return ReviewCommentResult.from(saved, author, replyCount);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedCommentResult query(org.yyubin.application.review.query.GetCommentsQuery query) {
        Review review = loadReviewPort.loadById(query.reviewId());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + query.reviewId());
        }
        if (!review.getVisibility().isPublic()) {
            if (query.viewerId() == null || !review.isWrittenBy(new UserId(query.viewerId()))) {
                throw new IllegalArgumentException("Review not found: " + query.reviewId());
            }
        }

        int fetchSize = query.size() + 1;
        var comments = loadReviewCommentPort.loadByReviewId(query.reviewId(), query.cursor(), fetchSize);

        // 전체 댓글 수 조회
        long totalCount = loadReviewCommentPort.countByReviewId(query.reviewId());

        // 대댓글 수 일괄 조회
        var commentIds = comments.stream()
                .limit(query.size())
                .map(c -> c.getId().getValue())
                .toList();
        var replyCounts = loadReviewCommentPort.countRepliesBatch(commentIds);

        var mapped = comments.stream()
                .limit(query.size())
                .map(comment -> {
                    org.yyubin.domain.user.User author = loadUserPort.loadById(comment.getUserId());
                    long replyCount = replyCounts.getOrDefault(comment.getId().getValue(), 0L);
                    return ReviewCommentResult.from(comment, author, replyCount);
                })
                .toList();

        Long nextCursor = comments.size() > query.size()
                ? comments.get(query.size()).getId().getValue()
                : null;

        return new PagedCommentResult(mapped, nextCursor, totalCount);
    }

    @Override
    @Transactional
    public ReviewCommentResult execute(UpdateCommentCommand command) {
        UserId writerId = new UserId(command.userId());
        ReviewComment comment = loadReviewCommentPort.loadById(command.commentId());

        if (!comment.isOwnedBy(writerId)) {
            throw new IllegalArgumentException("User is not the author of this comment");
        }
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Comment not found: " + command.commentId());
        }

        Review review = loadReviewPort.loadById(comment.getReviewId().getValue());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }
        if (!review.getVisibility().isPublic() && !review.isWrittenBy(writerId)) {
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }

        ReviewComment updated = comment.updateContent(command.content(), mentionParser.parse(command.content()));
        ReviewComment saved = saveReviewCommentPort.save(updated);

        org.yyubin.domain.user.User author = loadUserPort.loadById(writerId);
        long replyCount = saved.getParentId() == null ?
                loadReviewCommentPort.countRepliesByParentId(saved.getId().getValue()) : 0;
        return ReviewCommentResult.from(saved, author, replyCount);
    }

    @Override
    @Transactional
    public void execute(DeleteCommentCommand command) {
        UserId writerId = new UserId(command.userId());
        ReviewComment comment = loadReviewCommentPort.loadById(command.commentId());

        if (!comment.isOwnedBy(writerId)) {
            throw new IllegalArgumentException("User is not the author of this comment");
        }
        if (comment.isDeleted()) {
            throw new IllegalArgumentException("Comment not found: " + command.commentId());
        }

        Review review = loadReviewPort.loadById(comment.getReviewId().getValue());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + review.getId().getValue());
        }

        ReviewComment deleted = comment.markDeleted();
        saveReviewCommentPort.save(deleted);
        publishCommentEvent("COMMENT_DELETED", deleted, review);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedCommentResult query(org.yyubin.application.review.query.GetRepliesQuery query) {
        // 부모 댓글 존재 확인
        ReviewComment parentComment = loadReviewCommentPort.loadById(query.parentCommentId());
        if (parentComment.isDeleted()) {
            throw new IllegalArgumentException("Parent comment not found: " + query.parentCommentId());
        }

        // 리뷰 권한 확인
        Review review = loadReviewPort.loadById(parentComment.getReviewId().getValue());
        if (review.isDeleted()) {
            throw new IllegalArgumentException("Review not found: " + parentComment.getReviewId().getValue());
        }
        if (!review.getVisibility().isPublic()) {
            if (query.viewerId() == null || !review.isWrittenBy(new UserId(query.viewerId()))) {
                throw new IllegalArgumentException("Review not found: " + parentComment.getReviewId().getValue());
            }
        }

        int fetchSize = query.size() + 1;
        var replies = loadReviewCommentPort.loadRepliesByParentId(query.parentCommentId(), query.cursor(), fetchSize);

        // 전체 대댓글 수 조회
        long totalCount = loadReviewCommentPort.countRepliesByParentId(query.parentCommentId());

        var mapped = replies.stream()
                .limit(query.size())
                .map(reply -> {
                    org.yyubin.domain.user.User author = loadUserPort.loadById(reply.getUserId());
                    long replyCount = 0; // 대댓글은 replyCount가 0
                    return ReviewCommentResult.from(reply, author, replyCount);
                })
                .toList();

        Long nextCursor = replies.size() > query.size()
                ? replies.get(query.size()).getId().getValue()
                : null;

        return new PagedCommentResult(mapped, nextCursor, totalCount);
    }

    private void sendMentionNotifications(java.util.List<org.yyubin.domain.review.Mention> mentions, UserId writer, Long reviewId, Long commentId) {
        java.util.Set<Long> unique = new java.util.HashSet<>();
        for (org.yyubin.domain.review.Mention mention : mentions) {
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

    private void publishCommentEvent(String eventType, ReviewComment comment, Review review) {
        java.util.Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("reviewId", comment.getReviewId().getValue());
        metadata.put("bookId", review.getBookId().getValue());
        metadata.put("commentId", comment.getId() != null ? comment.getId().getValue() : null);

        eventPublisher.publish(
                EventTopics.REVIEW,
                comment.getUserId().value().toString(),
                new EventPayload(
                        java.util.UUID.randomUUID(),
                        eventType,
                        comment.getUserId().value(),
                        "REVIEW_COMMENT",
                        comment.getReviewId().getValue().toString(),
                        metadata,
                        java.time.Instant.now(),
                        "api",
                        1
                )
        );
    }
}
