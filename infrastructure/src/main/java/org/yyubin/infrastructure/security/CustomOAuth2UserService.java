package org.yyubin.infrastructure.security;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.yyubin.application.auth.port.LoadUserPort;
import org.yyubin.application.auth.port.SaveUserPort;
import org.yyubin.infrastructure.security.oauth2.CustomOAuth2User;
import org.yyubin.infrastructure.security.oauth2.OAuth2UserInfo;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo info = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        User user;
        try {
            user = loadUserPort.loadByEmail(info.getEmail());
        } catch (IllegalArgumentException e) {
            // 사용자가 없으면 새로 생성
            user = null;
        }

        if (user == null) {
            // OAuth2로 처음 로그인하는 사용자 생성
            User newUser = new User(
                    null,  // ID는 저장 시 자동 생성
                    info.getEmail(),
                    info.getName(),
                    "",  // OAuth2 사용자는 비밀번호 없음
                    "",  // bio는 기본값으로 빈 문자열
                    Role.USER,
                    mapToAuthProvider(registrationId),
                    LocalDateTime.now()
            );
            user = saveUserPort.save(newUser);
        }

        return new CustomOAuth2User(user.id().value(), user.email(), oAuth2User.getAttributes());
    }

    private AuthProvider mapToAuthProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> AuthProvider.GOOGLE;
            case "kakao" -> AuthProvider.KAKAO;
            case "naver" -> AuthProvider.NAVER;
            default -> AuthProvider.LOCAL;
        };
    }
}
