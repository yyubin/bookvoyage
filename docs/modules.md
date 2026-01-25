# 모듈 구조

## 모듈 개요

BookVoyage는 헥사고날 아키텍처를 기반으로 7개의 모듈로 구성됩니다.

```
bookvoyage/
├── api/                    # 🌐 REST API 엔드포인트
├── application/            # 💼 비즈니스 로직 (Use Cases, Ports)
├── domain/                 # 🧩 도메인 모델
├── infrastructure/         # 🔧 외부 시스템 어댑터
├── recommendation/         # 🤖 추천 시스템
├── batch/                  # ⏰ 배치 작업
└── support/                # 🛠️ 공통 유틸리티
```

## 🌐 API Module

### 역할
REST API 엔드포인트 제공 및 Spring Boot 애플리케이션 실행

### 주요 컨트롤러

#### AuthController
- `POST /api/auth/signup` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/oauth2/google` - Google 소셜 로그인

#### ReviewController
- `POST /api/reviews` - 리뷰 작성
- `GET /api/reviews/{id}` - 리뷰 조회
- `PUT /api/reviews/{id}` - 리뷰 수정
- `DELETE /api/reviews/{id}` - 리뷰 삭제
- `GET /api/reviews` - 리뷰 목록 조회

#### UserController
- `GET /api/users/{id}` - 사용자 프로필 조회
- `PUT /api/users/{id}` - 프로필 수정
- `GET /api/users/{id}/followers` - 팔로워 목록
- `GET /api/users/{id}/following` - 팔로잉 목록

#### FollowController
- `POST /api/users/{id}/follow` - 팔로우
- `DELETE /api/users/{id}/unfollow` - 언팔로우

#### NotificationController
- `GET /api/notifications` - 알림 목록 조회
- `PUT /api/notifications/{id}/read` - 알림 읽음 처리
- `PUT /api/notifications/settings` - 알림 설정 변경

#### RecommendationController
- `GET /api/recommendations/books` - 도서 추천
- `GET /api/recommendations/reviews` - 리뷰 추천
- `POST /api/recommendations/track` - 추천 이벤트 트래킹

#### BookSearchController
- `GET /api/books/search` - 도서 검색

### 책임
- HTTP 요청/응답 처리
- DTO 변환 (Request DTO → Command, Response DTO ← Domain)
- 인증/인가 (Spring Security)
- 예외 처리 (GlobalExceptionHandler)
- API 문서화 (SpringDoc OpenAPI)

## 💼 Application Module

### 역할
비즈니스 유스케이스 구현 및 Port 정의

### 주요 유스케이스

#### 리뷰 관련
- `CreateReviewUseCase` - 리뷰 작성
- `UpdateReviewUseCase` - 리뷰 수정
- `DeleteReviewUseCase` - 리뷰 삭제
- `GetReviewUseCase` - 리뷰 조회
- `ListReviewsUseCase` - 리뷰 목록 조회

#### 소셜 관련
- `FollowUserUseCase` - 사용자 팔로우
- `UnfollowUserUseCase` - 언팔로우
- `GetFollowersUseCase` - 팔로워 조회
- `GetFollowingUseCase` - 팔로잉 조회

#### 알림 관련
- `SendNotificationUseCase` - 알림 발송
- `MarkNotificationAsReadUseCase` - 알림 읽음 처리
- `UpdateNotificationSettingsUseCase` - 알림 설정 변경

### Port 정의

#### Output Ports (Infrastructure에서 구현)
```java
public interface SaveReviewPort {
    Review save(Review review);
}

public interface LoadReviewPort {
    Optional<Review> loadById(Long id);
}

public interface SendEventPort {
    void send(DomainEvent event);
}
```

#### Input Ports (Application에서 구현)
```java
public interface CreateReviewUseCase {
    ReviewResponse create(CreateReviewCommand command);
}
```

### 책임
- 비즈니스 로직 조율
- 트랜잭션 경계 관리 (`@Transactional`)
- 도메인 이벤트 발행
- Port를 통한 외부 시스템 연동

## 🧩 Domain Module

### 역할
순수 도메인 모델 및 비즈니스 규칙

### 주요 엔티티

#### User
- 사용자 정보
- 닉네임, 이메일, 비밀번호
- 프로필 이미지

#### Review
- 리뷰 내용
- 제목, 본문, 평점
- 공개 범위 (Public/Private)

#### Book
- 도서 정보
- 제목, 저자, ISBN, 장르

#### Notification
- 알림 정보
- 유형, 메시지, 읽음 여부

#### Follow
- 팔로우 관계
- 팔로워 ↔ 팔로잉

#### Reaction
- 리액션 (좋아요)
- 리뷰에 대한 좋아요

#### Bookmark
- 북마크
- 리뷰 북마크

#### Comment
- 댓글
- 리뷰에 대한 댓글 및 대댓글

### 도메인 이벤트
```java
public sealed interface DomainEvent permits
    ReviewCreatedEvent,
    ReviewDeletedEvent,
    UserFollowedEvent,
    ReactionAddedEvent {
}
```

### 비즈니스 규칙
- **PasswordPolicy**: 비밀번호 정책 검증
- **Review.changeVisibility()**: 공개 범위 변경 로직
- **User.updateProfile()**: 프로필 업데이트 로직

### 책임
- 순수 비즈니스 규칙
- 도메인 이벤트 정의
- 엔티티 생명주기 관리
- **외부 의존성 없음** (Framework 독립적)

## 🔧 Infrastructure Module

### 역할
외부 시스템 연동 및 Port 구현

### 주요 Adapter

#### Persistence Adapter (JPA)
```java
@Component
public class ReviewPersistenceAdapter implements SaveReviewPort, LoadReviewPort {
    private final ReviewJpaRepository repository;

    @Override
    public Review save(Review review) {
        ReviewEntity entity = ReviewMapper.toEntity(review);
        ReviewEntity saved = repository.save(entity);
        return ReviewMapper.toDomain(saved);
    }
}
```

#### Messaging Adapter (Kafka)
```java
@Component
public class KafkaEventPublisher implements SendEventPort {
    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    @Override
    public void send(DomainEvent event) {
        kafkaTemplate.send("domain-events", event);
    }
}
```

#### Cache Adapter (Redis)
```java
@Component
public class RedisCacheAdapter implements CachePort {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }
}
```

### 주요 구성요소
- **JPA Repositories**: MySQL 연동
- **Kafka Producer/Consumer**: 이벤트 스트리밍
- **Redis Operations**: 캐시 및 세션
- **Outbox Processor**: 메시지 신뢰성
- **Neo4j Template**: 그래프 DB 연동 (배치)
- **Elasticsearch Client**: 검색 엔진 연동 (배치)

### 책임
- Port 구현
- 외부 시스템 기술 세부사항 처리
- 엔티티 ↔ 도메인 모델 변환

## 🤖 Recommendation Module

### 역할
추천 시스템 엔진 (읽기 전용)

### 주요 구성요소

#### Candidate Generation
- **Neo4jCandidateGenerator**: 그래프 기반 후보 생성
- **ElasticsearchCandidateGenerator**: 검색 기반 후보 생성

#### Scorers
- **GraphScorer**: 그래프 관계 스코어
- **SemanticScorer**: 텍스트 유사도 스코어
- **PopularityScorer**: 인기도 스코어
- **FreshnessScorer**: 최신성 스코어

#### Sampling Strategies
- **NoShuffleStrategy**: 셔플 없음
- **WeightedRandomStrategy**: 가중치 무작위
- **WindowSamplingStrategy**: 윈도우 샘플링

### 데이터 소스
- **Neo4j**: MySQL의 파생 그래프 인덱스 (읽기 전용)
- **Elasticsearch**: MySQL의 파생 검색 인덱스 (읽기 전용)
- **Redis**: 추천 결과 캐싱 (TTL: 3시간)

### 책임
- 추천 후보 생성
- 하이브리드 스코어링
- 추천 결과 캐싱
- **데이터 쓰기 금지** (읽기 전용)

## ⏰ Batch Module

### 역할
정기 배치 작업 및 파생 인덱스 동기화

### 주요 배치 작업

#### Neo4j 동기화 (10분마다)
```java
@Scheduled(cron = "0 */10 * * * *")
public void syncNeo4j() {
    // MySQL → Neo4j 단방향 동기화
}
```

#### Elasticsearch 동기화 (30분마다)
```java
@Scheduled(cron = "0 */30 * * * *")
public void syncElasticsearch() {
    // MySQL → ES 단방향 동기화
}
```

#### 리뷰 조회수 플러시 (15분마다)
```java
@Scheduled(cron = "0 */15 * * * *")
public void flushViewCounts() {
    // Redis → MySQL 동기화
}
```

#### Outbox 정리 (매일 새벽 2시)
```java
@Scheduled(cron = "0 0 2 * * *")
public void cleanupOutbox() {
    // 7일 이상 오래된 이벤트 삭제
}
```

### 관리 기능
- **수동 배치 트리거 API**: 즉시 동기화 필요 시 사용
- **배치 실행 메타데이터**: 실행 시간, 상태, 처리 건수 추적
- **ShedLock**: 분산 락으로 중복 실행 방지

### 책임
- MySQL → Neo4j/ES 파생 인덱스 동기화
- Redis → MySQL 조회수 플러시
- Outbox 이벤트 정리

## 🛠️ Support Module

### 역할
공통 유틸리티 및 헬퍼 클래스

### 주요 구성요소

#### JWT Provider
```java
@Component
public class JwtProvider {
    public String generateAccessToken(User user);
    public String generateRefreshToken(User user);
    public Authentication getAuthentication(String token);
}
```

#### Nickname Generator
```java
@Component
public class NicknameGenerator {
    public String generate(); // 형용사 + 동물 조합
}
```

#### Exception Handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e);
}
```

#### 공통 DTO
- `SuccessResponse<T>`: 성공 응답 래퍼
- `ErrorResponse`: 에러 응답
- `PageResponse<T>`: 페이지네이션 응답

### 책임
- 공통 유틸리티 제공
- 예외 처리
- 응답 DTO 표준화
- JWT 토큰 관리

## 모듈 간 의존성

```
api → application → domain
api → support
application → infrastructure
application → recommendation
batch → infrastructure
batch → recommendation
infrastructure → domain
recommendation → domain
support (독립)
```

### 의존성 규칙
- **Domain**: 어떤 모듈에도 의존하지 않음 (순수)
- **Application**: Domain만 의존
- **Infrastructure**: Domain, Application 의존
- **API**: Application, Support 의존
- **Recommendation**: Domain만 의존
- **Batch**: Infrastructure, Recommendation 의존
