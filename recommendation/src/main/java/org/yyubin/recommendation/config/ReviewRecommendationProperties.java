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
}
