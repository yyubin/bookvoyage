# 데이터베이스 아키텍처

## MySQL = Source of Truth

**BookVoyage는 MySQL을 단일 진실 공급원(Single Source of Truth)으로 사용합니다.**

```
┌─────────────────────────────────────────────────────────┐
│                    MySQL (SoT)                          │
│  - 모든 도메인 데이터의 유일한 원본 저장소                    │
│  - 모든 쓰기(Create/Update/Delete) 작업은 MySQL에만 수행    │
│  - 트랜잭션 일관성 및 ACID 보장                            │
└─────────────────────────────────────────────────────────┘
                           │
                           │ Batch Sync (10분~30분 주기)
                           ▼
        ┌──────────────────────────────────────┐
        │                                      │
        ▼                                      ▼
┌──────────────────┐              ┌──────────────────────┐
│   Neo4j          │              │   Elasticsearch      │
│ (파생 그래프 인덱스) │              │  (파생 검색 인덱스)     │
│                  │              │                      │
│ - Read-only      │              │ - Read-only          │
│ - 추천 시스템 전용  │              │ - 검색 & MLT 전용     │
└──────────────────┘              └──────────────────────┘
```

## 데이터 동기화 전략

### 1. 쓰기: MySQL만 가능
- 모든 비즈니스 로직은 MySQL에 먼저 기록
- JPA 트랜잭션을 통한 데이터 무결성 보장
- Neo4j와 Elasticsearch는 직접 쓰기 금지

### 2. 읽기: 목적에 따라 분산
- **트랜잭션 조회**: MySQL (정확한 최신 데이터)
- **그래프 분석**: Neo4j (관계 기반 추천)
- **전문 검색**: Elasticsearch (텍스트 검색 및 유사도)

### 3. 동기화: 배치 작업
- **Neo4j 동기화**: 10분마다 (BatchSyncService)
- **Elasticsearch 동기화**: 30분마다 (BatchSyncService)
- **실패 처리**: 재시도 메커니즘 포함

### 4. 일관성 모델: Eventual Consistency
- 파생 인덱스는 최대 10-30분의 지연 가능
- 실시간 정확도가 필요한 경우 MySQL 직접 조회
- 추천 시스템은 약간의 지연 허용

## 데이터베이스별 역할

### MySQL 8.0 (Source of Truth)
**역할**: 모든 도메인 데이터의 원본 저장소

**주요 테이블**:
- **users**: 사용자 정보
- **reviews**: 리뷰 데이터
- **books**: 도서 정보
- **notifications**: 알림 데이터
- **follows**: 팔로우 관계
- **reactions**: 리액션(좋아요)
- **bookmarks**: 북마크
- **comments**: 댓글

**특징**:
- ACID 트랜잭션 보장
- JPA를 통한 ORM 매핑
- 인덱스 최적화 (@Table(indexes = ...))
- Soft Delete 패턴 (deleted_at)

### Neo4j 5.13 (파생 그래프 인덱스)
**역할**: 추천 시스템용 그래프 데이터베이스

**노드 타입**:
- **User**: 사용자 노드
- **Book**: 도서 노드
- **Review**: 리뷰 노드
- **Genre**: 장르 노드
- **Author**: 저자 노드

**관계 타입**:
- **REVIEWED**: User → Review → Book
- **FOLLOWS**: User → User
- **LIKES**: User → Review
- **HAS_GENRE**: Book → Genre
- **WRITTEN_BY**: Book → Author

**특징**:
- 읽기 전용 (Read-only)
- MySQL에서 배치 동기화 (10분마다)
- 그래프 기반 추천 알고리즘 (협업 필터링, k-hop)
- Cypher 쿼리 최적화

### Elasticsearch 8.11 (파생 검색 인덱스)
**역할**: 전문 검색 및 텍스트 유사도 분석

**인덱스**:
- **books**: 도서 검색
- **reviews**: 리뷰 검색
- **users**: 사용자 검색

**특징**:
- 읽기 전용 (Read-only)
- MySQL에서 배치 동기화 (30분마다)
- 전문 검색 (Full-text search)
- More Like This (MLT) 쿼리
- 텍스트 유사도 분석

### Redis 7 (Cache Layer)
**역할**: 캐시, 세션, 분산 락

**용도**:
- **캐시**: 추천 결과 (TTL: 3시간)
- **세션**: JWT 블랙리스트 (TTL: 토큰 만료 시간)
- **조회수**: 리뷰 조회수 임시 저장 (15분마다 MySQL로 플러시)
- **분산 락**: ShedLock (배치 작업 중복 실행 방지)

## 배치 동기화 작업

### Neo4j 동기화 (10분마다)
```java
@Scheduled(cron = "0 */10 * * * *")
public void syncNeo4j() {
    // 1. MySQL에서 변경된 데이터 조회
    // 2. Neo4j 노드/관계 생성/업데이트
    // 3. 실패 시 재시도
}
```

### Elasticsearch 동기화 (30분마다)
```java
@Scheduled(cron = "0 */30 * * * *")
public void syncElasticsearch() {
    // 1. MySQL에서 변경된 데이터 조회
    // 2. ES 인덱스 생성/업데이트
    // 3. 실패 시 재시도
}
```

### 리뷰 조회수 플러시 (15분마다)
```java
@Scheduled(cron = "0 */15 * * * *")
public void flushViewCounts() {
    // 1. Redis에서 조회수 조회
    // 2. MySQL에 반영
    // 3. Redis 삭제
}
```

## 데이터 일관성 보장

### Outbox Pattern
- MySQL에 비즈니스 데이터 + Outbox 이벤트를 동일 트랜잭션으로 저장
- OutboxProcessor가 별도로 Kafka로 발행
- 메시지 유실 방지

### Eventual Consistency
- 파생 인덱스(Neo4j, ES)는 최종 일관성 모델
- 10-30분의 지연 허용
- 실시간성이 중요한 조회는 MySQL 직접 사용

### 트랜잭션 경계
- **Application Layer**: JPA 트랜잭션 시작
- **Domain Layer**: 순수 비즈니스 로직 (트랜잭션 무관)
- **Infrastructure Layer**: 실제 DB 작업 수행

## 성능 최적화

### 인덱스 전략
- JPA `@Table(indexes = ...)`로 인덱스 선언
- 복합 인덱스: (user_id, created_at), (book_id, visibility)
- Full-text 인덱스: MySQL 8.0 FTS 활용 (필요 시)

### N+1 문제 해결
- `@EntityGraph`: Fetch Join
- `@BatchSize`: 배치 페치
- DTO Projection: 필요한 컬럼만 조회

### 커넥션 풀 설정
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
```
