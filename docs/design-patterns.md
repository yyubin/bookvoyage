# 주요 설계 패턴

## Hexagonal Architecture (Port & Adapter)

### 개요
도메인 로직을 외부 의존성으로부터 격리하여 비즈니스 규칙의 독립성을 보장합니다.

### Port
인터페이스로 비즈니스 로직과 외부 시스템 간의 계약을 정의합니다.

```java
// application/port/out/SaveReviewPort.java
public interface SaveReviewPort {
    Review save(Review review);
}

// application/port/out/LoadReviewPort.java
public interface LoadReviewPort {
    Optional<Review> loadById(Long id);
}

// application/port/in/CreateReviewUseCase.java
public interface CreateReviewUseCase {
    ReviewResponse create(CreateReviewCommand command);
}
```

### Adapter
Port 구현체로 실제 외부 시스템과 통신합니다.

```java
// infrastructure/persistence/ReviewPersistenceAdapter.java
@Component
public class ReviewPersistenceAdapter implements SaveReviewPort, LoadReviewPort {
    private final ReviewJpaRepository repository;

    @Override
    public Review save(Review review) {
        ReviewEntity entity = ReviewMapper.toEntity(review);
        ReviewEntity saved = repository.save(entity);
        return ReviewMapper.toDomain(saved);
    }

    @Override
    public Optional<Review> loadById(Long id) {
        return repository.findById(id)
            .map(ReviewMapper::toDomain);
    }
}
```

### 장점
- **테스트 용이성**: Mock Port로 쉽게 단위 테스트 가능
- **유연성**: 외부 시스템 교체 시 Adapter만 변경
- **도메인 순수성**: 도메인 로직이 프레임워크 독립적

## Outbox Pattern

### 개요
메시지 발행과 데이터베이스 변경을 하나의 트랜잭션으로 묶어 신뢰성을 보장합니다.

### 동작 흐름

#### 1. 비즈니스 로직 + Outbox 저장
```java
@Transactional
public void createReview(CreateReviewCommand cmd) {
    // 1. 비즈니스 데이터 저장 (MySQL)
    Review review = cmd.toReview();
    Review saved = saveReviewPort.save(review);

    // 2. Outbox 이벤트 저장 (같은 트랜잭션)
    OutboxEvent event = new OutboxEvent(
        "review-created",
        saved.getId(),
        EventStatus.PENDING
    );
    outboxPort.save(event);

    // 3. 트랜잭션 커밋 (둘 다 성공 또는 둘 다 실패)
}
```

#### 2. OutboxProcessor가 별도로 Kafka로 발행
```java
@Scheduled(fixedDelay = 1000) // 1초마다
public void processOutbox() {
    // 1. PENDING 상태 이벤트 조회
    List<OutboxEvent> pending = outboxPort.findPending(100);

    pending.forEach(event -> {
        try {
            // 2. Kafka로 발행
            kafkaTemplate.send(event.getTopic(), event.getPayload());

            // 3. 상태를 SENT로 변경
            event.markAsSent();
            outboxPort.save(event);
        } catch (Exception e) {
            // 4. 실패 시 재시도 (다음 배치에서)
            log.error("Failed to send event: {}", event.getId(), e);
        }
    });
}
```

### 장점
- **신뢰성**: 메시지 유실 방지 (DB 트랜잭션 보장)
- **일관성**: 비즈니스 데이터와 이벤트의 원자성
- **재시도**: 실패 시 자동 재시도

### 정리 작업
```java
@Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
public void cleanupOutbox() {
    LocalDateTime threshold = LocalDateTime.now().minusDays(7);
    outboxPort.deleteOlderThan(threshold);
}
```

## Dead Letter Queue (DLQ)

### 개요
재시도 후에도 실패한 메시지를 별도의 큐로 이동시켜 수동 처리합니다.

### 설정
```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> template) {
    // 1. DLQ로 발행하는 Recoverer
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(template,
            (record, ex) -> new TopicPartition(
                record.topic() + ".DLT", // Dead Letter Topic
                record.partition()
            )
        );

    // 2. 재시도 정책 (1초 간격, 5회)
    FixedBackOff backOff = new FixedBackOff(1000L, 5L);

    return new DefaultErrorHandler(recoverer, backOff);
}
```

### 동작 흐름
```
1. 메시지 처리 시도
2. 실패 → 1초 대기 → 재시도 (1/5)
3. 실패 → 1초 대기 → 재시도 (2/5)
...
6. 5회 모두 실패 → DLT 토픽으로 이동
```

### DLQ 메시지 처리
```java
@KafkaListener(topics = "review-events.DLT")
public void handleDeadLetter(ConsumerRecord<String, DomainEvent> record) {
    // 1. 로그 기록
    log.error("Dead letter message: topic={}, key={}, value={}",
        record.topic(), record.key(), record.value());

    // 2. 알림 발송 (관리자)
    alertService.sendAlert("DLQ message received");

    // 3. 수동 처리 또는 재발행 결정
}
```

## Repository Pattern with Specifications

### 개요
복잡한 쿼리를 재사용 가능한 Specification으로 분리합니다.

### Repository
```java
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long>,
                                          JpaSpecificationExecutor<ReviewEntity> {
    // 기본 CRUD는 JpaRepository에서 제공
    // 복잡한 쿼리는 Specification으로 분리
}
```

### Specification
```java
public class ReviewSpecifications {
    public static Specification<ReviewEntity> byUserId(Long userId) {
        return (root, query, cb) ->
            cb.equal(root.get("userId"), userId);
    }

    public static Specification<ReviewEntity> isPublic() {
        return (root, query, cb) ->
            cb.equal(root.get("visibility"), Visibility.PUBLIC);
    }

    public static Specification<ReviewEntity> notDeleted() {
        return (root, query, cb) ->
            cb.isNull(root.get("deletedAt"));
    }
}
```

### 사용
```java
// AND 조건으로 조합
Specification<ReviewEntity> spec = Specification
    .where(ReviewSpecifications.byUserId(userId))
    .and(ReviewSpecifications.isPublic())
    .and(ReviewSpecifications.notDeleted());

List<ReviewEntity> reviews = reviewRepository.findAll(spec);
```

### 장점
- **재사용성**: Specification을 조합하여 다양한 쿼리 생성
- **가독성**: 복잡한 쿼리를 명확한 이름으로 표현
- **타입 안전성**: 컴파일 타임 오류 검출

## Chain of Responsibility (책임 연쇄 패턴)

### 개요
추천 결과 셔플 전략을 체인으로 구성하여 유연하게 전환합니다.

### 전략 인터페이스
```java
public interface ShuffleStrategy {
    List<Item> shuffle(List<Item> items, ShuffleContext context);
}
```

### 구현체

#### 1. NoShuffleStrategy
```java
public class NoShuffleStrategy implements ShuffleStrategy {
    @Override
    public List<Item> shuffle(List<Item> items, ShuffleContext context) {
        // 셔플 없이 스코어 순서 유지
        return items;
    }
}
```

#### 2. WeightedRandomStrategy
```java
public class WeightedRandomStrategy implements ShuffleStrategy {
    @Override
    public List<Item> shuffle(List<Item> items, ShuffleContext context) {
        // 스코어를 가중치로 사용하여 무작위 셔플
        return weightedRandomShuffle(items);
    }
}
```

#### 3. WindowSamplingStrategy
```java
public class WindowSamplingStrategy implements ShuffleStrategy {
    @Override
    public List<Item> shuffle(List<Item> items, ShuffleContext context) {
        // 윈도우 샘플링으로 다양성 보장
        int windowSize = context.getWindowSize();
        return windowSample(items, windowSize);
    }
}
```

### 사용
```java
// 전략 선택
ShuffleStrategy strategy = switch (recommendationType) {
    case PERSONALIZED -> new WindowSamplingStrategy();
    case POPULAR -> new NoShuffleStrategy();
    case RANDOM -> new WeightedRandomStrategy();
};

// 전략 실행
List<Item> result = strategy.shuffle(candidates, context);
```

### 장점
- **유연성**: 런타임에 전략 교체 가능
- **확장성**: 새로운 전략 추가 용이
- **단일 책임**: 각 전략이 하나의 셔플 방식만 담당

## Cursor-based Pagination

### 개요
오프셋 기반 페이지네이션의 문제를 해결하기 위해 커서 기반 방식을 사용합니다.

### 응답 구조
```java
public record RecommendationPage<T>(
    List<T> items,
    String nextCursor,      // 다음 페이지 커서
    boolean hasNext         // 다음 페이지 존재 여부
) {}
```

### 구현
```java
public RecommendationPage<Book> getRecommendations(
    String cursor,
    int limit
) {
    // 1. 커서 디코딩
    Long lastId = decodeCursor(cursor);

    // 2. 커서 이후 데이터 조회
    List<Book> books = bookRepository.findAllByIdGreaterThan(
        lastId,
        PageRequest.of(0, limit + 1) // +1개 조회
    );

    // 3. hasNext 계산
    boolean hasNext = books.size() > limit;
    if (hasNext) {
        books = books.subList(0, limit);
    }

    // 4. nextCursor 생성
    String nextCursor = hasNext
        ? encodeCursor(books.get(books.size() - 1).getId())
        : null;

    return new RecommendationPage<>(books, nextCursor, hasNext);
}
```

### 커서 인코딩/디코딩
```java
private String encodeCursor(Long id) {
    return Base64.getEncoder().encodeToString(
        id.toString().getBytes()
    );
}

private Long decodeCursor(String cursor) {
    if (cursor == null) return 0L;
    return Long.parseLong(
        new String(Base64.getDecoder().decode(cursor))
    );
}
```

### 장점
- **성능**: 오프셋 스캔 없이 직접 시작 지점 찾기
- **일관성**: 실시간 데이터 변경에 강건
- **무한 스크롤**: 프론트엔드 무한 스크롤에 최적화

### 단점
- **임의 페이지 접근 불가**: 특정 페이지 번호로 이동 불가
- **정렬 제약**: 커서 필드는 유니크하고 정렬 가능해야 함

## Domain Event Pattern

### 개요
도메인 내에서 발생한 중요한 사건을 이벤트로 표현합니다.

### 이벤트 정의
```java
public sealed interface DomainEvent permits
    ReviewCreatedEvent,
    ReviewDeletedEvent,
    UserFollowedEvent,
    ReactionAddedEvent {

    String getEventType();
    LocalDateTime getOccurredAt();
}
```

### 이벤트 발행
```java
@Transactional
public class ReviewService {
    public Review createReview(CreateReviewCommand cmd) {
        // 1. 비즈니스 로직
        Review review = cmd.toReview();
        Review saved = saveReviewPort.save(review);

        // 2. 도메인 이벤트 발행
        DomainEvent event = new ReviewCreatedEvent(
            saved.getId(),
            saved.getUserId(),
            saved.getBookId(),
            LocalDateTime.now()
        );
        eventPublisher.publish(event);

        return saved;
    }
}
```

### 이벤트 핸들러
```java
@Component
public class NotificationEventHandler {
    @EventListener
    public void handleReviewCreated(ReviewCreatedEvent event) {
        // 팔로워들에게 알림 발송
        sendNotificationUseCase.notifyFollowers(event.getUserId());
    }
}
```

### 장점
- **느슨한 결합**: 이벤트 발행자와 구독자 분리
- **확장성**: 새로운 핸들러 추가 용이
- **감사 추적**: 도메인 이벤트 히스토리 기록
