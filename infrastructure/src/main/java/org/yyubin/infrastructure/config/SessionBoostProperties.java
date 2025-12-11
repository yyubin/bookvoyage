package org.yyubin.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "session.boost")
public class SessionBoostProperties {
    private long ttlSeconds = 3600;
    private int maxEntries = 100;
}
