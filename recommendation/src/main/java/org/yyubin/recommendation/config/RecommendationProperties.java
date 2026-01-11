package org.yyubin.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 추천 시스템 설정 Properties
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    private GraphConfig graph = new GraphConfig();
    private SearchConfig search = new SearchConfig();
    private ScoringConfig scoring = new ScoringConfig();
    private CacheConfig cache = new CacheConfig();

    @Getter
    @Setter
    public static class GraphConfig {
        private int batchSize = 100;
        private int maxHops = 2;
    }

    @Getter
    @Setter
    public static class SearchConfig {
        private int maxCandidates = 500;
        private double minScore = 0.1;
    }

    @Getter
    @Setter
    public static class ScoringConfig {
        private Weights weights = new Weights();

        @Getter
        @Setter
        public static class Weights {
            private double graph = 0.4;
            private double semantic = 0.3;
            private double popularity = 0.1;
            private double freshness = 0.05;
        }
    }

    @Getter
    @Setter
    public static class CacheConfig {
        private int ttlHours = 3;
    }
}
