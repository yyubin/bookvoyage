package org.yyubin.recommendation.review;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "recommendation.review.highlight")
@Getter
@Setter
public class ReviewHighlightRecommendationProperties {

    private int maxCandidates = 300;
    private double esWeight = 0.6;
    private double graphWeight = 0.4;
}
