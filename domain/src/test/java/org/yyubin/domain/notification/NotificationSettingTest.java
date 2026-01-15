package org.yyubin.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yyubin.domain.user.UserId;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationSetting domain tests")
class NotificationSettingTest {

    @Test
    @DisplayName("defaultFor enables all settings")
    void defaultForEnablesAll() {
        NotificationSetting setting = NotificationSetting.defaultFor(new UserId(1L));

        assertThat(setting.isLikeAndCommentEnabled()).isTrue();
        assertThat(setting.isMentionEnabled()).isTrue();
        assertThat(setting.isFolloweeReviewEnabled()).isTrue();
    }

    @Test
    @DisplayName("toggle methods change flags")
    void togglesChangeFlags() {
        NotificationSetting setting = NotificationSetting.of(new UserId(1L), false, false, false);

        setting.enableLikeAndComment();
        setting.enableMention();
        setting.enableFolloweeReview();

        assertThat(setting.isLikeAndCommentEnabled()).isTrue();
        assertThat(setting.isMentionEnabled()).isTrue();
        assertThat(setting.isFolloweeReviewEnabled()).isTrue();

        setting.disableLikeAndComment();
        setting.disableMention();
        setting.disableFolloweeReview();

        assertThat(setting.isLikeAndCommentEnabled()).isFalse();
        assertThat(setting.isMentionEnabled()).isFalse();
        assertThat(setting.isFolloweeReviewEnabled()).isFalse();
    }
}
