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
import org.yyubin.application.dto.AuthResult;
import org.yyubin.domain.user.AuthProvider;
import org.yyubin.domain.user.Role;
import org.yyubin.domain.user.User;
import org.yyubin.domain.user.UserId;
import org.yyubin.support.jwt.JwtProvider;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginService 테스트")
class LoginServiceTest {

    @Mock
    private LoadUserPort loadUserPort;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private LoginService loginService;

    private User testUser;
    private String email;
    private String password;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        email = "test@example.com";
        password = "password123";
        encodedPassword = "encodedPassword123";

        testUser = new User(
                new UserId(1L),
                email,
                "testuser",
                encodedPassword,
                "Test User",
                "Test bio",
                "",
                Role.USER,
                AuthProvider.LOCAL,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("정상적인 로그인 성공")
    void execute_Success() {
        // Given
        String accessToken = "access.token.here";
        String refreshToken = "refresh.token.here";

        when(loadUserPort.loadByEmail(email)).thenReturn(testUser);
        when(passwordEncoderPort.matches(password, encodedPassword)).thenReturn(true);
        when(jwtProvider.createAccessToken(
                eq("1"),
                eq(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        )).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken("1")).thenReturn(refreshToken);

        // When
        AuthResult result = loginService.execute(email, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.username()).isEqualTo("testuser");

        verify(loadUserPort).loadByEmail(email);
        verify(passwordEncoderPort).matches(password, encodedPassword);
        verify(jwtProvider).createAccessToken(eq("1"), any());
        verify(jwtProvider).createRefreshToken("1");
    }

    @Test
    @DisplayName("비밀번호 불일치로 로그인 실패")
    void execute_FailWithInvalidPassword() {
        // Given
        when(loadUserPort.loadByEmail(email)).thenReturn(testUser);
        when(passwordEncoderPort.matches(password, encodedPassword)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> loginService.execute(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid password");

        verify(loadUserPort).loadByEmail(email);
        verify(passwordEncoderPort).matches(password, encodedPassword);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인 실패")
    void execute_FailWithNonExistentUser() {
        // Given
        when(loadUserPort.loadByEmail(email)).thenThrow(new IllegalArgumentException("User not found"));

        // When & Then
        assertThatThrownBy(() -> loginService.execute(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");

        verify(loadUserPort).loadByEmail(email);
    }
}
