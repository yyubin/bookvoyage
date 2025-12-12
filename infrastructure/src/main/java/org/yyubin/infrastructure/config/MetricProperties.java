package org.yyubin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "metric.review.view")
public class MetricProperties {
    private long counterTtlSeconds = 86400;
    private long dedupTtlSeconds = 86400;
}
