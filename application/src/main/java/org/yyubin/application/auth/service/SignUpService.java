package org.yyubin.application.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.yyubin.application.auth.SignUpUseCase;
import org.yyubin.application.auth.port.LoadUserPort;
import org.yyubin.application.auth.port.PasswordEncoderPort;
import org.yyubin.application.auth.port.SaveUserPort;
import org.yyubin.application.dto.AuthResult;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.PasswordPolicy;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.support.jwt.JwtProvider;
import org.yyubin.support.nickname.NicknameGenerator;

@Service
@RequiredArgsConstructor
public class SignUpService implements SignUpUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JwtProvider jwtProvider;
    private final NotificationSettingPort notificationSettingPort;

    @Override
    public AuthResult execute(String email, String password, String username, String bio) {

        // 비밀번호 정책 검증
        PasswordPolicy.validate(password);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoderPort.encode(password);

        // User 도메인 객체 생성
        User newUser = new User(
                null,  // ID는 저장 시 자동 생성
                email,
                username,
                encodedPassword,
                NicknameGenerator.generate(email),
                bio,
                null,
                Role.USER,
                AuthProvider.LOCAL,
                null,
                LocalDateTime.now()
        );

        // User 저장
        User savedUser = saveUserPort.save(newUser);
        notificationSettingPort.save(NotificationSetting.defaultFor(savedUser.id()));

        String accessToken = jwtProvider.createAccessToken(
                savedUser.id().value().toString(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String refreshToken = jwtProvider.createRefreshToken(
                savedUser.id().value().toString()
        );

        return new AuthResult(
                accessToken,
                refreshToken,
                savedUser.id().value(),
                savedUser.email(),
                savedUser.username()
        );
    }

    private String makeRandomNickname(String email) {
        String nickname;
        int attempt = 0;

        do {
            String seed = attempt == 0 ? email : email + "#" + attempt;
            nickname = NicknameGenerator.generate(seed);
            attempt++;
        } while (loadUserPort.loadByNickname(nickname).isPresent());

        return nickname;
    }
}
