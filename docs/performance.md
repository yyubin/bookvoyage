# 성능 최적화

## 캐싱 전략

### Redis 캐싱

#### 추천 결과 캐싱
```java
@Service
public class RecommendationCacheService {
    private static final Duration RECOMMENDATION_TTL = Duration.ofHours(3);

    public List<Book> getOrCompute(String cacheKey, Supplier<List<Book>> supplier) {
        // 1. 캐시 조회
        List<Book> cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 2. 계산 수행
        List<Book> result = supplier.get();

        // 3. 캐시 저장 (TTL: 3시간)
        redisTemplate.opsForValue().set(cacheKey, result, RECOMMENDATION_TTL);

        return result;
    }
}
```

#### 캐시 키 전략
```java
// 사용자별 도서 추천
String cacheKey = String.format(
    "recommendations:books:user:%d:type:%s",
    userId,
    recommendationType
);

// 도서별 리뷰 추천
String cacheKey = String.format(
    "recommendations:reviews:book:%d:strategy:%s",
    bookId,
    strategy
);
```

#### TTL 설정
| 캐시 타입 | TTL | 이유 |
|---------|-----|------|
| 추천 결과 | 3시간 | 개인화 데이터, 주기적 갱신 필요 |
| 세션 | 2주 | Refresh Token 유효기간과 동일 |
| JWT 블랙리스트 | 토큰 만료 시간 | 만료 후 자동 정리 |
| 리뷰 조회수 | 15분 | 배치 플러시 주기와 동일 |

### 캐시 워밍 (Cache Warming)
```java
@Scheduled(cron = "0 0 * * * *") // 매시간
public void warmCache() {
    // 인기 사용자 추천 결과 미리 생성
    List<Long> popularUserIds = getPopularUserIds();

    popularUserIds.forEach(userId -> {
        recommendationService.getRecommendations(userId);
    });
}
```

### 캐시 무효화 (Cache Invalidation)
```java
@Service
public class UserService {
    @Transactional
    public void updateUserProfile(Long userId, UpdateProfileCommand cmd) {
        // 1. 프로필 업데이트
        userPort.update(userId, cmd);

        // 2. 추천 캐시 무효화
        String pattern = String.format("recommendations:*:user:%d:*", userId);
        redisTemplate.delete(redisTemplate.keys(pattern));
    }
}
```

## 데이터베이스 최적화

### 인덱스 전략

#### 단일 컬럼 인덱스
```java
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_book_id", columnList = "book_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class ReviewEntity {
    // ...
}
```

#### 복합 인덱스
```java
@Entity
@Table(name = "reviews", indexes = {
    // 사용자별 최신 리뷰 조회 (user_id, created_at DESC)
    @Index(name = "idx_user_created", columnList = "user_id, created_at"),

    // 도서별 공개 리뷰 조회 (book_id, visibility, created_at)
    @Index(name = "idx_book_visibility_created",
           columnList = "book_id, visibility, created_at"),

    // Soft Delete 조회 (deleted_at IS NULL)
    @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
public class ReviewEntity {
    // ...
}
```

#### 인덱스 선택 가이드
```sql
-- ✅ 인덱스 활용 (idx_user_created 사용)
SELECT * FROM reviews
WHERE user_id = 123
ORDER BY created_at DESC
LIMIT 10;

-- ❌ 인덱스 미활용 (함수 사용)
SELECT * FROM reviews
WHERE DATE(created_at) = '2024-01-01';

-- ✅ 개선 (범위 조건 사용)
SELECT * FROM reviews
WHERE created_at >= '2024-01-01 00:00:00'
  AND created_at < '2024-01-02 00:00:00';
```

### N+1 문제 해결

#### 1. @EntityGraph (Fetch Join)
```java
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    @EntityGraph(attributePaths = {"user", "book"})
    List<ReviewEntity> findAllByUserId(Long userId);
}
```

**Before (N+1)**:
```sql
SELECT * FROM reviews WHERE user_id = 123;          -- 1번
SELECT * FROM users WHERE id = ?;                   -- N번
SELECT * FROM books WHERE id = ?;                   -- N번
```

**After (Fetch Join)**:
```sql
SELECT r.*, u.*, b.*
FROM reviews r
LEFT JOIN users u ON r.user_id = u.id
LEFT JOIN books b ON r.book_id = b.id
WHERE r.user_id = 123;                              -- 1번
```

#### 2. @BatchSize
```java
@Entity
public class UserEntity {
    @OneToMany(mappedBy = "user")
    @BatchSize(size = 100)
    private List<ReviewEntity> reviews;
}
```

**Before**:
```sql
SELECT * FROM reviews WHERE user_id = 1;  -- N번
SELECT * FROM reviews WHERE user_id = 2;
...
```

**After (Batch Fetch)**:
```sql
SELECT * FROM reviews
WHERE user_id IN (1, 2, 3, ..., 100);     -- 100개씩 묶어서 조회
```

#### 3. DTO Projection
```java
public interface ReviewSummaryProjection {
    Long getId();
    String getTitle();
    Integer getRating();
    // Book과 User는 조회하지 않음
}

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewSummaryProjection> findAllProjectedByUserId(Long userId);
}
```

### 배치 설정
```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 100            # 배치 삽입/업데이트
        order_inserts: true          # INSERT 정렬 (배치 효율 증대)
        order_updates: true          # UPDATE 정렬
        default_batch_fetch_size: 100  # 배치 페치
```

## Kafka 최적화

### Consumer 동시성 설정
```yaml
spring:
  kafka:
    consumer:
      concurrency: 3  # 3개 스레드로 병렬 처리
```

### 배치 처리
```java
@KafkaListener(topics = "domain-events")
public void consumeBatch(List<DomainEvent> events) {
    // 100개씩 묶어서 처리
    events.forEach(event -> {
        // 이벤트 처리
    });
}
```

### Producer 설정
```yaml
spring:
  kafka:
    producer:
      batch-size: 16384          # 배치 크기 (바이트)
      linger-ms: 10              # 배치 대기 시간 (ms)
      compression-type: snappy   # 압축 (네트워크 대역폭 절약)
```

### 재시도 설정
```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate template) {
    // 1초 간격, 5회 재시도
    FixedBackOff backOff = new FixedBackOff(1000L, 5L);
    return new DefaultErrorHandler(recoverer, backOff);
}
```

## 분산 락

### Redisson 분산 락
```java
@Service
public class ReviewService {
    private final RedissonClient redisson;

    public void incrementViewCount(Long reviewId) {
        String lockKey = "lock:review:" + reviewId;
        RLock lock = redisson.getLock(lockKey);

        try {
            // 락 획득 (최대 10초 대기, 5초 후 자동 해제)
            if (lock.tryLock(10, 5, TimeUnit.SECONDS)) {
                try {
                    // 조회수 증가 로직
                    Long count = redisTemplate.opsForValue()
                        .increment("viewCount:review:" + reviewId);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### ShedLock 배치 락
```java
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
public class SchedulerConfig {
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory);
    }
}

@Component
public class BatchJob {
    @Scheduled(cron = "0 */10 * * * *")
    @SchedulerLock(
        name = "syncNeo4j",
        lockAtMostFor = "9m",    // 최대 락 유지 시간
        lockAtLeastFor = "1m"    // 최소 락 유지 시간
    )
    public void syncNeo4j() {
        // 배치 작업 (중복 실행 방지)
    }
}
```

## 커넥션 풀 최적화

### HikariCP 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10      # 최대 커넥션 수
      minimum-idle: 5            # 최소 유휴 커넥션
      connection-timeout: 20000  # 커넥션 대기 시간 (ms)
      idle-timeout: 300000       # 유휴 커넥션 제거 시간 (5분)
      max-lifetime: 1800000      # 커넥션 최대 수명 (30분)
```

### 커넥션 풀 사이징 공식
```
최적 커넥션 수 = CPU 코어 수 × 2 + 디스크 수

예: 4 코어 CPU + 1 디스크 = 4 × 2 + 1 = 9개
→ 여유를 두어 10개로 설정
```

## 쿼리 성능 모니터링

### Hibernate 통계
```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true  # 통계 생성
        session:
          events:
            log:
              LOG_QUERIES_SLOWER_THAN_MS: 1000  # 1초 이상 쿼리 로깅
```

### 슬로우 쿼리 로깅
```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG                     # SQL 로깅
    org.hibernate.type.descriptor.sql: TRACE     # 파라미터 로깅
    org.hibernate.stat: DEBUG                    # 통계 로깅
```

### JPA 쿼리 카운트 검증 (테스트)
```java
@Test
public void shouldNotHaveNPlusOne() {
    // Given
    Long userId = 123L;

    // When
    List<Review> reviews = reviewService.getUserReviews(userId);

    // Then: 쿼리가 3번 이하여야 함 (N+1 문제 없음)
    assertThat(getQueryCount()).isLessThanOrEqualTo(3);
}
```

## API 응답 최적화

### DTO Projection
```java
// ❌ 불필요한 데이터 조회
public ReviewResponse getReview(Long id) {
    Review review = reviewRepository.findById(id).orElseThrow();
    return ReviewMapper.toResponse(review); // 모든 필드 반환
}

// ✅ 필요한 데이터만 조회
public ReviewSummaryResponse getReviewSummary(Long id) {
    return reviewRepository.findSummaryById(id).orElseThrow();
}
```

### 페이지네이션
```java
// ❌ 전체 데이터 조회
public List<Review> getAllReviews() {
    return reviewRepository.findAll(); // 메모리 부족 위험
}

// ✅ 페이지네이션
public Page<Review> getReviews(Pageable pageable) {
    return reviewRepository.findAll(pageable);
}

// ✅ 커서 기반 (무한 스크롤)
public RecommendationPage<Review> getReviews(String cursor, int limit) {
    return reviewService.getReviewsAfter(cursor, limit);
}
```

### 압축
```yaml
server:
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
    min-response-size: 1024  # 1KB 이상일 때만 압축
```

## 성능 테스트

### JMeter
```bash
# 동시 사용자 100명, 10분간 테스트
jmeter -n -t load-test.jmx -l results.jtl
```

### K6
```javascript
import http from 'k6/http';

export const options = {
  stages: [
    { duration: '2m', target: 100 },  // Ramp-up
    { duration: '5m', target: 100 },  // Stay
    { duration: '2m', target: 0 },    // Ramp-down
  ],
};

export default function () {
  http.get('http://localhost:8080/api/recommendations/books');
}
```
