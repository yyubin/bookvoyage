package org.yyubin.support.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "test-account")
public class TestAccountProperties {

    private String emailDomain;

    public boolean isTestEmail(String email) {
        if (emailDomain == null || emailDomain.isBlank()) {
            return false;
        }
        if (email == null || email.isBlank()) {
            return false;
        }
        return email.toLowerCase().endsWith("@" + emailDomain.toLowerCase());
    }
}
