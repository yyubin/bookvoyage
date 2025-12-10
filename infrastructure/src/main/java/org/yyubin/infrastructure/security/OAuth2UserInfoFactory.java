package org.yyubin.infrastructure.security;

import java.util.Map;
import org.yyubin.infrastructure.security.oauth2.GoogleOAuth2UserInfo;
import org.yyubin.infrastructure.security.oauth2.KakaoOAuth2UserInfo;
import org.yyubin.infrastructure.security.oauth2.NaverOAuth2UserInfo;
import org.yyubin.infrastructure.security.oauth2.OAuth2UserInfo;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);
        };
    }
}
