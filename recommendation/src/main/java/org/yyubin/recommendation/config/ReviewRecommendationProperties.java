package org.yyubin.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 리뷰 추천 설정
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "recommendation.review")
public class ReviewRecommendationProperties {

    private int maxCandidates = 300;
    private Cache cache = new Cache();
    private Scoring scoring = new Scoring();
    private Search search = new Search();

    @Getter
    @Setter
    public static class Cache {
        private int ttlHours = 2;
        private int maxItems = 100;
    }

    @Getter
    @Setter
    public static class Scoring {
        private Weights weights = new Weights();

        @Getter
        @Setter
        public static class Weights {
            private double popularity = 0.35;
            private double freshness = 0.15;
            private double engagement = 0.2;
            private double content = 0.2;
            private double bookContext = 0.1;
        }
    }

    @Getter
    @Setter
    public static class Search {
        private int contextReviewLimit = 5;
        private int contextLibraryLimit = 5;
        private int contextSearchLimit = 5;
        private int contextSearchDays = 30;
        private double feedInterestRatio = 0.6;
        private double feedMltRatio = 0.3;
        private double feedSemanticRatio = 0.3;
        private int seedLimit = 3;
        private int exposureTtlHours = 24;
        private int exposureMaxItems = 200;
        private int exposureFilterLimit = 200;
        private double feedGraphRatio = 0.3;
        private double graphSimilarRatio = 0.6;
        private int graphBookSeedLimit = 20;
    }
}
