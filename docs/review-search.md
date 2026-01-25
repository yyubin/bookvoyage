## 리뷰 검색 (Elasticsearch)

### 개요
리뷰 검색은 Elasticsearch 기반이며 `GET /api/reviews/search`로 노출됩니다.
현재 경로는 키워드 기반 검색(멀티매치 + ngram + 키워드 와일드카드 부스트)이며,
벡터/임베딩 기반 시맨틱 검색은 사용하지 않습니다.

흐름:
`ReviewSearchController` -> `ReviewSearchService` -> `ReviewSearchAdapter` -> Elasticsearch (`review_content` 인덱스)

### 엔드포인트
`GET /api/reviews/search`

필수:
- `q` (String): 키워드. 공백은 허용하지 않음.

페이지네이션:
- `cursor` (Long, 선택): 이전 페이지의 마지막 `reviewId`. `reviewId < cursor` 조건으로 사용.
- `size` (Integer, 선택): 기본 10, 최대 30, 최소 1.

필터:
- `genre` (String)
- `minRating` / `maxRating` (Integer, 1..5)
- `startDate` / `endDate` (ISO date, 날짜 시작 시각으로 변환)
- `highlight` (String) -> 정규화 후 `highlightsNorm`와 매칭
- `bookId` (Long)
- `userId` (Long)

정렬 (`sortBy`):
- `RELEVANCE` (기본): `_score desc`, 이후 `reviewId desc`
- `LATEST`: `createdAt desc`, 이후 `reviewId desc`
- `RATING_DESC`: `rating desc`, 이후 `reviewId desc`
- `RATING_ASC`: `rating asc`, 이후 `reviewId desc`

### 쿼리 동작
필터 유무와 상관없이 기본 쿼리는 JSON `bool` 형태입니다.
- `should`:
  - 가중치 멀티매치:
    - `bookTitle^4`, `summary^3`, `highlights^2`, `content^1`
    - ngram 서브필드:
      - `bookTitle.ngram^0.8`, `summary.ngram^0.5`, `highlights.ngram^0.5`, `content.ngram^0.3`
  - `keywords` 필드에 토큰별 trailing `*` 와일드카드, 부스트 3.0
- `minimum_should_match: 1`
- 필요 시 `filter`:
  - `reviewId < cursor`
  - `genre`, `rating` 범위, `createdAt` 범위, `highlightsNorm`, `bookId`, `userId`

비고:
- 와일드카드는 trailing-only로 제한합니다(선행 `*` 미사용).
- `highlight` 필터는 `highlightsNorm`를 기준으로 합니다(인덱싱 시 정규화).

### 커서 페이지네이션
`reviewId < cursor` 조건으로 페이징하며, 마지막 `reviewId`를 `nextCursor`로 반환합니다.
`createdAt`이나 `rating` 정렬에서도 커서는 `reviewId` 기준이므로 정렬 안정성은 정렬 옵션에 따라 달라질 수 있습니다.

### 인덱스 및 매핑
인덱스명: `review_content`

문서 필드:
- `reviewId`, `userId`, `bookId`, `authorNickname`
- `bookTitle`, `summary`, `content`
- `highlights`, `highlightsNorm`, `keywords`
- `genre`, `createdAt`, `rating`

분석기:
- `nori_analyzer` (한국어) 적용: `bookTitle`, `summary`, `content`, `highlights`
- `ngram_analyzer` (2~3그램) 적용: `.ngram` 서브필드

매핑/설정 위치:
- `recommendation/src/main/resources/elasticsearch/review-mappings.json`
- `recommendation/src/main/resources/elasticsearch/review-settings.json`

### 인덱싱 흐름 (쓰기 경로)
- `ReviewService`에서 리뷰 생성/수정/삭제 시 `ReviewSearchIndexEvent` 발행
- recommendation 모듈의 Kafka consumer가 수신 후 `review_content` 인덱스에 반영

### 제한 사항
- 벡터/임베딩 기반 시맨틱 검색은 사용하지 않습니다.
- 키워드 기반 텍스트 검색이며, fuzziness/동의어 설정은 없습니다.
- 커서는 항상 `reviewId` 기준이므로 비관련도 정렬에서는 페이징 안정성이 낮을 수 있습니다.
