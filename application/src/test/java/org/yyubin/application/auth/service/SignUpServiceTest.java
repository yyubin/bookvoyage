package org.yyubin.application.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.yyubin.application.auth.port.LoadUserPort;
import org.yyubin.application.auth.port.PasswordEncoderPort;
import org.yyubin.application.auth.port.SaveUserPort;
import org.yyubin.application.dto.AuthResult;
import org.yyubin.application.notification.port.NotificationSettingPort;
import org.yyubin.domain.notification.NotificationSetting;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.InvalidPasswordException;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;
import org.yyubin.support.jwt.JwtProvider;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SignUpService 테스트")
class SignUpServiceTest {

    @Mock
    private SaveUserPort saveUserPort;

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private NotificationSettingPort notificationSettingPort;

    @InjectMocks
    private SignUpService signUpService;

    private String email;
    private String password;
    private String username;
    private String bio;

    @BeforeEach
    void setUp() {
        email = "newuser@example.com";
        password = "Password123!";
        username = "newuser";
        bio = "New user bio";
    }

    @Test
    @DisplayName("정상적인 회원가입 성공")
    void execute_Success() {
        // Given
        String encodedPassword = "encodedPassword123";
        String accessToken = "access.token.here";
        String refreshToken = "refresh.token.here";

        User savedUser = new User(
                new UserId(1L),
                email,
                username,
                encodedPassword,
                "GeneratedNickname",
                bio,
                "",
                Role.USER,
                AuthProvider.LOCAL,
                null,
                LocalDateTime.now()
        );

        when(passwordEncoderPort.encode(password)).thenReturn(encodedPassword);
        when(saveUserPort.save(any(User.class))).thenReturn(savedUser);
        when(notificationSettingPort.save(any(NotificationSetting.class)))
                .thenReturn(NotificationSetting.defaultFor(savedUser.id()));
        when(jwtProvider.createAccessToken(
                eq("1"),
                eq(List.of(new SimpleGrantedAuthority("ROLE_USER")))
        )).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken("1")).thenReturn(refreshToken);

        // When
        AuthResult result = signUpService.execute(email, password, username, bio);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.username()).isEqualTo(username);

        verify(passwordEncoderPort).encode(password);
        verify(saveUserPort).save(any(User.class));
        verify(notificationSettingPort).save(any(NotificationSetting.class));
        verify(jwtProvider).createAccessToken(eq("1"), anyList());
        verify(jwtProvider).createRefreshToken("1");
    }

    @Test
    @DisplayName("비밀번호 정책 위반 - 너무 짧은 비밀번호")
    void execute_FailWithShortPassword() {
        // Given
        String weakPassword = "Pass1!";

        // When & Then
        assertThatThrownBy(() -> signUpService.execute(email, weakPassword, username, bio))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 정책 위반 - 대문자 없음")
    void execute_FailWithoutUppercase() {
        // Given
        String weakPassword = "password123!";

        // When & Then
        assertThatThrownBy(() -> signUpService.execute(email, weakPassword, username, bio))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 정책 위반 - 소문자 없음")
    void execute_FailWithoutLowercase() {
        // Given
        String weakPassword = "PASSWORD123!";

        // When & Then
        assertThatThrownBy(() -> signUpService.execute(email, weakPassword, username, bio))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 정책 위반 - 숫자 없음")
    void execute_FailWithoutDigit() {
        // Given
        String weakPassword = "Password!";

        // When & Then
        assertThatThrownBy(() -> signUpService.execute(email, weakPassword, username, bio))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 정책 위반 - 특수문자 없음")
    void execute_FailWithoutSpecialChar() {
        // Given
        String weakPassword = "Password123";

        // When & Then
        assertThatThrownBy(() -> signUpService.execute(email, weakPassword, username, bio))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 정책 위반 - null 비밀번호")
    void execute_FailWithNullPassword() {
        // When & Then
        assertThatThrownBy(() -> signUpService.execute(email, null, username, bio))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
