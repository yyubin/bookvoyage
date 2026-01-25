# 보안

## 인증 시스템

### OAuth2 + JWT 기반 인증
- **Access Token**: 1시간 유효기간
- **Refresh Token**: 7일 유효기간
- **HttpOnly 쿠키**: 토큰 전달 (XSS 공격 방지)
- **SameSite=Strict**: CSRF 공격 방지

### 인증 흐름

#### 회원가입
```
1. 사용자 → POST /api/auth/signup (이메일, 비밀번호)
2. 비밀번호 정책 검증
3. BCrypt 해싱 후 MySQL에 저장
4. Access Token + Refresh Token 발급
5. HttpOnly 쿠키로 토큰 전달
```

#### 로그인
```
1. 사용자 → POST /api/auth/login (이메일, 비밀번호)
2. 비밀번호 검증
3. Access Token + Refresh Token 발급
4. HttpOnly 쿠키로 토큰 전달
```

#### 로그아웃
```
1. 사용자 → POST /api/auth/logout
2. JWT 블랙리스트에 Access Token + Refresh Token 추가
3. Redis TTL: 토큰 만료 시간까지
4. 쿠키 삭제
```

#### OAuth2 소셜 로그인 (Google)
```
1. 사용자 → GET /api/auth/oauth2/google
2. Google 인증 페이지로 리다이렉트
3. 콜백 처리 (사용자 정보 조회)
4. 신규 사용자면 자동 회원가입
5. Access Token + Refresh Token 발급
6. HttpOnly 쿠키로 토큰 전달
```

## JWT 블랙리스트

### 개요
로그아웃 시 토큰을 무효화하기 위해 Redis 기반 블랙리스트를 사용합니다.

### 동작 원리
```java
// 로그아웃 시 토큰 무효화
tokenBlacklistService.addToBlacklist(accessToken);
tokenBlacklistService.addToBlacklist(refreshToken);

// 인증 필터에서 블랙리스트 체크
if (tokenBlacklistService.isBlacklisted(token)) {
    throw new UnauthorizedException("Token has been revoked");
}
```

### 주요 기능

#### JTI (JWT ID) 기반 추적
- 각 토큰에 고유 ID 할당
- Redis에 JTI를 키로 저장
- 빠른 조회 성능 (O(1))

#### Redis TTL 자동 만료
```java
// Access Token: 1시간 TTL
redisTemplate.opsForValue().set(
    "blacklist:access:" + jti,
    "revoked",
    Duration.ofHours(1)
);

// Refresh Token: 7일 TTL
redisTemplate.opsForValue().set(
    "blacklist:refresh:" + jti,
    "revoked",
    Duration.ofDays(7)
);
```

#### 사용자 전체 토큰 무효화
비밀번호 변경 또는 계정 탈퇴 시:
```java
// 사용자의 모든 토큰 무효화
tokenBlacklistService.revokeAllUserTokens(userId);
```

### Redis 키 구조
```
blacklist:access:<jti>  → "revoked" (TTL: 1시간)
blacklist:refresh:<jti> → "revoked" (TTL: 7일)
user:tokens:<userId>    → Set<jti> (사용자별 토큰 목록)
```

## 비밀번호 정책

### 정책 규칙
- **최소 길이**: 8자 이상
- **대문자**: 1개 이상 (A-Z)
- **소문자**: 1개 이상 (a-z)
- **숫자**: 1개 이상 (0-9)
- **특수문자**: 1개 이상 (@$!%*?&)

### 도메인 계층 검증
```java
// domain/user/PasswordPolicy.java
public class PasswordPolicy {
    public static void validate(String password) {
        if (password.length() < 8) {
            throw new InvalidPasswordException("비밀번호는 8자 이상이어야 합니다");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new InvalidPasswordException("대문자를 1개 이상 포함해야 합니다");
        }
        // ... (나머지 검증)
    }
}
```

### BCrypt 해싱
```java
// infrastructure/security/PasswordEncoder.java
@Component
public class PasswordEncoder {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
```

## 환경 변수 보안

### 필수 환경 변수
```bash
# JWT 시크릿 (256비트 이상)
JWT_SECRET=your-secret-key-here

# DB 비밀번호
DB_PASSWORD=your-secure-password

# OAuth2 클라이언트 시크릿
GOOGLE_CLIENT_SECRET=your-google-client-secret

# AWS S3 (프로덕션)
AWS_S3_SECRET_KEY=your-s3-secret-key
```

### .env 파일 관리
```bash
# .env.example을 복사하여 .env 생성
cp .env.example .env

# .env 파일은 .gitignore에 추가
echo ".env" >> .gitignore
```

### JWT 시크릿 생성
```bash
# 안전한 랜덤 시크릿 생성 (256비트)
openssl rand -base64 32
```

## API 보안

### 인증 필터
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) {
        // 1. 쿠키에서 토큰 추출
        String token = extractTokenFromCookie(request);

        // 2. 블랙리스트 체크
        if (blacklistService.isBlacklisted(token)) {
            throw new UnauthorizedException();
        }

        // 3. 토큰 검증 및 인증 처리
        Authentication auth = jwtProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
```

### CORS 설정
```java
@Configuration
public class WebSecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // 프론트엔드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true); // 쿠키 허용
        return source;
    }
}
```

### Rate Limiting
추후 추가 예정 (Redis 기반 Rate Limiter)

## XSS 방지

### HttpOnly 쿠키
- JavaScript에서 토큰 접근 불가
- XSS 공격으로 토큰 탈취 방지

### Content Security Policy
추후 추가 예정

## CSRF 방지

### SameSite 쿠키
```java
Cookie cookie = new Cookie("accessToken", token);
cookie.setHttpOnly(true);
cookie.setSecure(true); // HTTPS only
cookie.setSameSite("Strict"); // CSRF 방지
```

### CSRF 토큰
Spring Security CSRF 토큰 (필요 시 활성화)

## SQL Injection 방지

### JPA 파라미터 바인딩
```java
// ✅ 안전: JPA 파라미터 바인딩
@Query("SELECT r FROM Review r WHERE r.userId = :userId")
List<Review> findByUserId(@Param("userId") Long userId);

// ❌ 위험: 문자열 연결s
// NEVER DO THIS
@Query("SELECT r FROM Review r WHERE r.userId = " + userId)
```

### 입력 검증
- `@Valid` 애노테이션
- DTO 레벨 검증
- 도메인 레벨 검증
