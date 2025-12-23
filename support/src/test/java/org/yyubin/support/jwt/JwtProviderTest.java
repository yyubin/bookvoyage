package org.yyubin.support.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtProvider 테스트")
class JwtProviderTest {

    private JwtProvider jwtProvider;
    private JwtProperties jwtProperties;
    private static final String TEST_SECRET = "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm-requirements";
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7일

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(TEST_SECRET);
        jwtProperties.setAccessTokenExpiration(ACCESS_TOKEN_EXPIRATION);
        jwtProperties.setRefreshTokenExpiration(REFRESH_TOKEN_EXPIRATION);

        jwtProvider = new JwtProvider(jwtProperties);
    }

    @Test
    @DisplayName("액세스 토큰 생성 성공")
    void createAccessToken_Success() {
        // Given
        String userId = "12345";
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        // When
        String token = jwtProvider.createAccessToken(userId, authorities);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 파싱하여 검증
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("auth", String.class)).isEqualTo("ROLE_USER,ROLE_ADMIN");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 성공")
    void createRefreshToken_Success() {
        // Given
        String userId = "12345";

        // When
        String token = jwtProvider.createRefreshToken(userId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();

        // 토큰 파싱하여 검증
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(userId);
        assertThat(claims.get("auth")).isNull(); // 리프레시 토큰에는 권한 정보 없음
    }

    @Test
    @DisplayName("유효한 토큰 검증 성공")
    void validateToken_ValidToken_ReturnsTrue() {
        // Given
        String userId = "12345";
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtProvider.createAccessToken(userId, authorities);

        // When
        boolean isValid = jwtProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 검증 실패")
    void validateToken_MalformedToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.format";

        // When
        boolean isValid = jwtProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 검증 실패")
    void validateToken_InvalidSignature_ReturnsFalse() {
        // Given
        String wrongSecret = "wrong-secret-key-that-is-different-from-the-original-secret-key-used";
        SecretKey wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithWrongSignature = Jwts.builder()
                .subject("12345")
                .claim("auth", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .signWith(wrongKey)
                .compact();

        // When
        boolean isValid = jwtProvider.validateToken(tokenWithWrongSignature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void validateToken_ExpiredToken_ReturnsFalse() {
        // Given
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("12345")
                .claim("auth", "ROLE_USER")
                .issuedAt(new Date(System.currentTimeMillis() - 10000))
                .expiration(new Date(System.currentTimeMillis() - 5000)) // 5초 전에 만료
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtProvider.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("토큰으로부터 Authentication 객체 생성 성공")
    void getAuthentication_Success() {
        // Given
        String userId = "12345";
        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        String token = jwtProvider.createAccessToken(userId, authorities);

        // When
        Authentication authentication = jwtProvider.getAuthentication(token);

        // Then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(userId);
        assertThat(authentication.getAuthorities()).hasSize(2);
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("토큰으로부터 사용자 ID 추출 성공")
    void getUserIdFromToken_Success() {
        // Given
        String userId = "12345";
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtProvider.createAccessToken(userId, authorities);

        // When
        String extractedUserId = jwtProvider.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("빈 권한으로 토큰 생성 성공")
    void createAccessToken_WithEmptyAuthorities_Success() {
        // Given
        String userId = "12345";
        Collection<GrantedAuthority> authorities = List.of();

        // When
        String token = jwtProvider.createAccessToken(userId, authorities);

        // Then
        assertThat(token).isNotNull();

        Authentication authentication = jwtProvider.getAuthentication(token);
        assertThat(authentication.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("동일한 입력으로 생성한 토큰은 매번 다름 (시간 포함)")
    void createAccessToken_DifferentTokensEachTime() throws InterruptedException {
        // Given
        String userId = "12345";
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

        // When
        String token1 = jwtProvider.createAccessToken(userId, authorities);
        Thread.sleep(1000); // 1초 대기하여 issuedAt 시간 차이 보장
        String token2 = jwtProvider.createAccessToken(userId, authorities);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("리프레시 토큰의 만료 시간이 액세스 토큰보다 긺")
    void refreshToken_HasLongerExpiration() {
        // Given
        String userId = "12345";

        // When
        String accessToken = jwtProvider.createAccessToken(userId, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        String refreshToken = jwtProvider.createRefreshToken(userId);

        // Then
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));

        Claims accessClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();

        Claims refreshClaims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();

        assertThat(refreshClaims.getExpiration()).isAfter(accessClaims.getExpiration());
    }
}
