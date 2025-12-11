package org.yyubin.application.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.yyubin.application.auth.LoginUseCase;
import org.yyubin.application.auth.port.LoadUserPort;
import org.yyubin.application.auth.port.PasswordEncoderPort;
import org.yyubin.application.dto.AuthResult;
import org.yyubin.domain.user.User;
import org.yyubin.support.jwt.JwtProvider;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

    private final LoadUserPort loadUserPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final JwtProvider jwtProvider;

    @Override
    public AuthResult execute(String email, String password) {

        // 1. 사용자 조회
        User user = loadUserPort.loadByEmail(email);

        // 2. 비밀번호 검증
        if (!passwordEncoderPort.matches(password, user.password())) {
            throw new IllegalArgumentException("Invalid password");
        }

        // 3. 권한 생성 (일단 USER 고정)
        var authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_USER")
        );

        // 4. JWT 발급
        String accessToken = jwtProvider.createAccessToken(
                String.valueOf(user.id().value()),
                authorities
        );

        String refreshToken = jwtProvider.createRefreshToken(
                String.valueOf(user.id().value())
        );

        // 5. AuthResult 생성 후 반환
        return new AuthResult(
                accessToken,
                refreshToken,
                user.id().value(),
                user.email(),
                user.username()
        );
    }
}
