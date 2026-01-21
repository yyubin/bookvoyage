package org.yyubin.batch.sync;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("ReviewEngagementStats 테스트")
class ReviewEngagementStatsTest {

    @Test
    @DisplayName("CTR 계산 - 정상 케이스")
    void ctr_NormalCase() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(1000, 800, 100, 50000, 50);

        // When
        float ctr = stats.ctr();

        // Then
        assertThat(ctr).isCloseTo(0.1f, within(0.001f));
    }

    @Test
    @DisplayName("CTR 계산 - impressions가 0일 때")
    void ctr_ZeroImpressions() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(0, 0, 10, 0, 0);

        // When
        float ctr = stats.ctr();

        // Then
        assertThat(ctr).isEqualTo(0f);
    }

    @Test
    @DisplayName("CTR 계산 - impressions가 음수일 때")
    void ctr_NegativeImpressions() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(-100, 0, 10, 0, 0);

        // When
        float ctr = stats.ctr();

        // Then
        assertThat(ctr).isEqualTo(0f);
    }

    @Test
    @DisplayName("도달율 계산 - 정상 케이스")
    void reachRate_NormalCase() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(1000, 500, 100, 50000, 50);

        // When
        float reachRate = stats.reachRate();

        // Then
        assertThat(reachRate).isCloseTo(0.5f, within(0.001f));
    }

    @Test
    @DisplayName("도달율 계산 - impressions가 0일 때")
    void reachRate_ZeroImpressions() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(0, 500, 100, 50000, 50);

        // When
        float reachRate = stats.reachRate();

        // Then
        assertThat(reachRate).isEqualTo(0f);
    }

    @Test
    @DisplayName("평균 체류시간 계산 - 정상 케이스")
    void avgDwellMs_NormalCase() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(1000, 500, 100, 50000, 10);

        // When
        long avgDwell = stats.avgDwellMs();

        // Then
        assertThat(avgDwell).isEqualTo(5000L);
    }

    @Test
    @DisplayName("평균 체류시간 계산 - dwellCount가 0일 때")
    void avgDwellMs_ZeroDwellCount() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(1000, 500, 100, 50000, 0);

        // When
        long avgDwell = stats.avgDwellMs();

        // Then
        assertThat(avgDwell).isEqualTo(0L);
    }

    @Test
    @DisplayName("평균 체류시간 계산 - dwellCount가 음수일 때")
    void avgDwellMs_NegativeDwellCount() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(1000, 500, 100, 50000, -5);

        // When
        long avgDwell = stats.avgDwellMs();

        // Then
        assertThat(avgDwell).isEqualTo(0L);
    }

    @Test
    @DisplayName("모든 값이 0인 경우")
    void allZeroValues() {
        // Given
        ReviewEngagementStats stats = new ReviewEngagementStats(0, 0, 0, 0, 0);

        // When & Then
        assertThat(stats.ctr()).isEqualTo(0f);
        assertThat(stats.reachRate()).isEqualTo(0f);
        assertThat(stats.avgDwellMs()).isEqualTo(0L);
    }

    @Test
    @DisplayName("레코드 동등성 테스트")
    void recordEquality() {
        // Given
        ReviewEngagementStats stats1 = new ReviewEngagementStats(100, 80, 10, 5000, 5);
        ReviewEngagementStats stats2 = new ReviewEngagementStats(100, 80, 10, 5000, 5);

        // Then
        assertThat(stats1).isEqualTo(stats2);
        assertThat(stats1.hashCode()).isEqualTo(stats2.hashCode());
    }
}
