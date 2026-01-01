package org.yyubin.application.activity.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.application.activity.GetActivityFeedUseCase;
import org.yyubin.application.activity.dto.ActivityActorResult;
import org.yyubin.application.activity.dto.ActivityFeedItemResult;
import org.yyubin.application.activity.dto.ActivityFeedPageResult;
import org.yyubin.application.activity.port.ActivityQueryPort;
import org.yyubin.application.activity.query.GetActivityFeedQuery;
import org.yyubin.application.review.LoadHighlightsUseCase;
import org.yyubin.application.review.LoadKeywordsUseCase;
import org.yyubin.application.review.dto.ReviewResult;
import org.yyubin.application.review.port.LoadBookPort;
import org.yyubin.application.review.port.LoadReviewPort;
import org.yyubin.application.user.port.FollowQueryPort;
import org.yyubin.domain.activity.ActivityItem;
import org.yyubin.domain.review.Review;
import org.yyubin.domain.review.ReviewVisibility;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityFeedQueryService implements GetActivityFeedUseCase {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final ActivityQueryPort activityQueryPort;
    private final FollowQueryPort followQueryPort;
    private final LoadReviewPort loadReviewPort;
    private final LoadBookPort loadBookPort;
    private final LoadKeywordsUseCase loadKeywordsUseCase;
    private final LoadHighlightsUseCase loadHighlightsUseCase;
    private final org.yyubin.application.user.port.LoadUserPort loadUserPort;

    @Override
    public ActivityFeedPageResult query(GetActivityFeedQuery query) {
        int size = resolveSize(query.size());
        LocalDateTime cursor = toDateTime(query.cursorEpochMillis());

        List<Long> followingIds = followQueryPort.loadFollowingIdsAll(query.userId());
        List<ActivityItem> activities =
                activityQueryPort.loadActivities(followingIds, query.userId(), cursor, size + 1);

        List<ActivityFeedItemResult> mapped = activities.stream()
                .limit(size)
                .map(this::toResult)
                .filter(java.util.Objects::nonNull)
                .toList();

        Long nextCursor = activities.size() > size
                ? activities.get(size).getCreatedAt().toInstant(ZoneOffset.UTC).toEpochMilli()
                : null;

        return new ActivityFeedPageResult(mapped, nextCursor);
    }

    private ActivityFeedItemResult toResult(ActivityItem item) {
        User actor;
        try {
            actor = loadUserPort.loadById(item.getActorId());
        } catch (IllegalArgumentException ex) {
            return null;
        }
        ActivityActorResult actorResult = new ActivityActorResult(
                actor.id().value(),
                actor.username(),
                actor.nickname(),
                actor.ProfileImageUrl()
        );

        ReviewResult reviewResult = null;
        if (item.getReviewId() != null) {
            Review review;
            try {
                review = loadReviewPort.loadById(item.getReviewId().getValue());
            } catch (IllegalArgumentException ex) {
                return null;
            }
            if (review.isDeleted() || review.getVisibility() != ReviewVisibility.PUBLIC) {
                return null;
            }
            var book = loadBookPort.loadById(review.getBookId().getValue())
                    .orElseThrow(() -> new IllegalArgumentException("Book not found: " + review.getBookId().getValue()));
            var author = loadUserPort.loadById(review.getUserId());
            reviewResult = ReviewResult.from(
                    review,
                    book,
                    author,
                    loadKeywordsUseCase.loadKeywords(review.getId()),
                    loadHighlightsUseCase.loadHighlights(review.getId())
            );
        }

        return new ActivityFeedItemResult(
                item.getId(),
                item.getType(),
                item.getCreatedAt(),
                actorResult,
                reviewResult
        );
    }

    private LocalDateTime toDateTime(Long cursorEpochMillis) {
        if (cursorEpochMillis == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(cursorEpochMillis), ZoneOffset.UTC);
    }

    private int resolveSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        return Math.min(Math.max(size, 1), MAX_SIZE);
    }
}
