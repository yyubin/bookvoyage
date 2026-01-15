package org.yyubin.domain.search;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TrendingKeyword record tests")
class TrendingKeywordTest {

    @Test
    @DisplayName("of sets stable trend")
    void ofSetsStableTrend() {
        TrendingKeyword keyword = TrendingKeyword.of("key", 100, 1);

        assertThat(keyword.trend()).isEqualTo(TrendingKeyword.TrendDirection.STABLE);
    }

    @Test
    @DisplayName("withTrend overrides trend")
    void withTrendOverrides() {
        TrendingKeyword keyword = TrendingKeyword.of("key", 100, 1);
        TrendingKeyword updated = keyword.withTrend(TrendingKeyword.TrendDirection.UP);

        assertThat(updated.trend()).isEqualTo(TrendingKeyword.TrendDirection.UP);
    }
}
