package org.yyubin.support.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.frontend")
public class FrontendProperties {
    /**
     * 프론트엔드 기본 URL (OAuth2 리다이렉트 등에 사용)
     * 예: https://bookvoyage.com
     */
    private String url = "http://localhost:3000";

    /**
     * CORS 허용 origins (쉼표로 구분)
     * 예: https://bookvoyage.com,https://www.bookvoyage.com
     */
    private List<String> corsOrigins = List.of("http://localhost:3000");

    /**
     * OAuth2 인증 성공 후 리다이렉트 경로
     */
    private String oauth2RedirectPath = "/oauth2/redirect";

    /**
     * OAuth2 리다이렉트 전체 URL 반환
     */
    public String getOauth2RedirectUrl() {
        return url + oauth2RedirectPath;
    }
}
