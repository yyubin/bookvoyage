package org.yyubin.domain.recommendation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CommunityTrend record tests")
class CommunityTrendTest {

    @Test
    @DisplayName("of builds record")
    void ofBuildsRecord() {
        CommunityTrend.TrendingGenre genre = CommunityTrend.TrendingGenre.of("fantasy", 0.3, "up");
        CommunityTrend trend = CommunityTrend.of(List.of("magic"), "summary", List.of(genre));

        assertThat(trend.keywords()).contains("magic");
        assertThat(trend.analyzedAt()).isNotNull();
        assertThat(trend.genres()).hasSize(1);
    }

    @Test
    @DisplayName("trending genre factory builds record")
    void trendingGenreFactory() {
        CommunityTrend.TrendingGenre genre = CommunityTrend.TrendingGenre.of("romance", 0.2, "steady");

        assertThat(genre.genre()).isEqualTo("romance");
        assertThat(genre.percentage()).isEqualTo(0.2);
    }
}
