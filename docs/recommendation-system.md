# 추천 시스템

> **중요**: 추천 시스템은 **MySQL의 파생 인덱스**(Neo4j, Elasticsearch)를 활용합니다.
> 모든 추천 데이터는 MySQL에서 배치 동기화되며, 추천 엔진은 읽기 전용으로 동작합니다.

## 2-Stage 추천 파이프라인

### Stage 1: Candidate Generation (후보 생성)
```
Neo4j (파생 그래프 인덱스): 협업 필터링 + 장르/저자 기반 → 250개
Elasticsearch (파생 검색 인덱스): MLT + 시맨틱 + 인기 도서 → 250개
  - MLT/시맨틱은 최근 검색어와 최근 서재/리뷰 기반 시드를 사용
────────────────────────────────────────────────────────────
합계: 500개 후보 (중복 제거 후)
```

### Stage 2: Scoring & Ranking (스코어링)
```
도서 추천:
  HybridScorer = GraphScore(40%) + SemanticScore(30%) +
                 PopularityScore(10%) + FreshnessScore(5%)

리뷰 추천:
  ReviewHybridScorer = PopularityScore(30%) + FreshnessScore(25%) +
                       EngagementScore(20%) + ContentScore(15%) +
                       BookContextScore(10%)
```

**결과**: Top 50개를 Redis에 캐싱 (3시간)

### 추가 기능
- 커서 기반 페이지네이션으로 무한 스크롤 지원
- 윈도우 샘플링으로 추천 다양성 보장
- 실시간 이벤트 반영 (클릭, 체류, 스크롤 등)

## 지원하는 추천 알고리즘

### 도서 추천 (7가지 알고리즘)
- **협업 필터링** (User-based CF)
- **장르 기반 추천**
- **저자 기반 추천**
- **그래프 k-hop 유사 도서**
- **More Like This** (Elasticsearch)
- **시맨틱 검색 추천**
- **인기 도서 추천**

### 리뷰 추천 (5가지 알고리즘)
- **피드용 맞춤 리뷰**
- **도서별 베스트 리뷰**
- **인기/최신 리뷰**
- **품질 기반 리뷰**

## 추천 알고리즘별 데이터 소스

| 추천 유형 | 알고리즘 | 데이터 소스 |
|----------|---------|-----------|
| 협업 필터링 | User-based CF | Neo4j (파생 인덱스) |
| 장르 기반 | Content-based | Neo4j (파생 인덱스) |
| 저자 기반 | Content-based | Neo4j (파생 인덱스) |
| 유사 도서 | Graph k-hop | Neo4j (파생 인덱스) |
| More Like This | Text similarity | Elasticsearch (파생 인덱스) |
| 인기 도서 | Popularity-based | Elasticsearch (파생 인덱스) |
| 시맨틱 검색 | Text search | Elasticsearch (파생 인덱스) |

**데이터 원본**: 모든 추천 데이터는 **MySQL(SoT)**에서 배치 동기화됩니다.

## 추천 개인화

### 실시간 이벤트 트래킹
- **IMPRESSION**: 추천 항목 노출
- **CLICK**: 추천 항목 클릭
- **DWELL**: 체류 시간
- **SCROLL**: 스크롤 깊이
- **BOOKMARK**: 북마크 추가
- **LIKE**: 좋아요

### 윈도우 샘플링 전략
다양성을 보장하기 위해 윈도우 샘플링 방식을 사용합니다.
- 상위 추천 결과를 작은 윈도우로 분할
- 각 윈도우에서 무작위로 항목 선택
- 필터 버블 방지 및 serendipity 증대

### 책임 연쇄 패턴 기반 셔플 전략
- **NoShuffleStrategy**: 셔플 없이 스코어 순서 유지
- **WeightedRandomStrategy**: 가중치 기반 무작위 셔플
- **WindowSamplingStrategy**: 윈도우 샘플링 셔플

## 커서 기반 페이지네이션

무한 스크롤을 지원하기 위해 커서 기반 페이지네이션을 사용합니다.

```java
public record RecommendationPage<T>(
    List<T> items,
    String nextCursor,
    boolean hasNext
) {}
```

- 오프셋 기반보다 성능 우수
- 실시간 데이터 변경에 강건
- 중복/누락 방지

## 캐싱 전략

- **Redis TTL**: 3시간
- **캐시 키**: 사용자 ID + 추천 타입 + 파라미터
- **캐시 워밍**: 배치 동기화 후 인기 사용자 캐시 미리 생성
- **캐시 무효화**: 사용자 프로필 변경 시

