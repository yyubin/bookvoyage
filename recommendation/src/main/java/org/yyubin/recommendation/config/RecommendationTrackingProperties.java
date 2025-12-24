package org.yyubin.recommendation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "recommendation.tracking")
public class RecommendationTrackingProperties {

    private Weights weights = new Weights();
    private Caps caps = new Caps();

    @Getter
    @Setter
    public static class Weights {
        private double impression = 0.05;
        private double click = 0.3;
        private double dwellPerMs = 0.001;
        private double scrollPerPct = 0.005;
        private double bookmark = 1.0;
        private double like = 0.6;
        private double follow = 0.8;
        private double reviewCreate = 0.4;
        private double reviewUpdate = 0.4;
    }

    @Getter
    @Setter
    public static class Caps {
        private double dwellMax = 1.5;
        private double scrollMax = 0.5;
    }
}
