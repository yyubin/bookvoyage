package org.yyubin.domain.notification;

import org.yyubin.domain.user.UserId;

public class NotificationSetting {

    private final UserId userId;
    private boolean likeAndCommentEnabled;
    private boolean mentionEnabled;
    private boolean followeeReviewEnabled;

    private NotificationSetting(UserId userId,
                               boolean likeAndCommentEnabled,
                               boolean mentionEnabled,
                               boolean followeeReviewEnabled) {
        this.userId = userId;
        this.likeAndCommentEnabled = likeAndCommentEnabled;
        this.mentionEnabled = mentionEnabled;
        this.followeeReviewEnabled = followeeReviewEnabled;
    }

    public static NotificationSetting defaultFor(UserId userId) {
        return new NotificationSetting(userId, true, true, true);
    }

    public static NotificationSetting of(UserId userId,
                                         boolean likeAndCommentEnabled,
                                         boolean mentionEnabled,
                                         boolean followeeReviewEnabled) {
        return new NotificationSetting(userId, likeAndCommentEnabled, mentionEnabled, followeeReviewEnabled);
    }

    public UserId getUserId() {
        return userId;
    }

    public boolean isLikeAndCommentEnabled() {
        return likeAndCommentEnabled;
    }

    public boolean isMentionEnabled() {
        return mentionEnabled;
    }

    public boolean isFolloweeReviewEnabled() {
        return followeeReviewEnabled;
    }

    public void enableLikeAndComment() { this.likeAndCommentEnabled = true; }
    public void disableLikeAndComment() { this.likeAndCommentEnabled = false; }

    public void enableMention() { this.mentionEnabled = true; }
    public void disableMention() { this.mentionEnabled = false; }

    public void enableFolloweeReview() { this.followeeReviewEnabled = true; }
    public void disableFolloweeReview() { this.followeeReviewEnabled = false; }
}
