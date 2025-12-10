package org.yyubin.infrastructure.persistence.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.UserId;

@Entity
@Table(name = "notification_setting")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationSettingEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "like_comment_enabled", nullable = false)
    private boolean likeCommentEnabled;

    @Column(name = "mention_enabled", nullable = false)
    private boolean mentionEnabled;

    @Column(name = "followee_review_enabled", nullable = false)
    private boolean followeeReviewEnabled;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public NotificationSetting toDomain() {
        return NotificationSetting.of(
                new UserId(userId),
                likeCommentEnabled,
                mentionEnabled,
                followeeReviewEnabled
        );
    }

    public static NotificationSettingEntity fromDomain(NotificationSetting domain) {
        return NotificationSettingEntity.builder()
                .userId(domain.getUserId().value())
                .likeCommentEnabled(domain.isLikeAndCommentEnabled())
                .mentionEnabled(domain.isMentionEnabled())
                .followeeReviewEnabled(domain.isFolloweeReviewEnabled())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
