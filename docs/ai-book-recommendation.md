## AI 책 추천 기능 조사 (사용자 취향 분석 + 추천 설명)

### 1) 기능 개요
AI 관련 기능은 크게 두 가지로 구성됩니다.
- **사용자 취향 분석(페르소나 + 키워드 + AI 추천 도서)**: `/api/ai/user-analysis`
- **추천 설명 생성(추천 사유 문장)**: 추천 결과에 대한 문장 생성

LLM은 OpenAI(기본 `gpt-4o-mini`)를 사용하며, 캐시/DB를 통해 **하루 1회** 분석 결과를 재사용합니다.

### 2) 호출 지점과 사용 흐름
**사용자 취향 분석**
- API: `GET /api/ai/user-analysis`
- 처리 흐름:
  - `AIController` -> `UserPreferenceAnalysisService`
  - 컨텍스트 수집 -> 캐시 확인 -> DB 확인 -> LLM 호출

**추천 설명**
- 서비스 내부에서 `AIEnrichmentService.generateExplanation()` 호출
- 책 제목/점수 정보를 받아 LLM 설명 생성 (실패 시 무시)

### 3) LLM에 전달되는 데이터 (트래킹/취향 데이터)
사용자 취향 분석은 **최근 활동 컨텍스트**를 JSON으로 만들어 LLM에 전달합니다.

수집 항목과 개수는 고정 값으로 정의되어 있습니다.
- 최근 리뷰: **최대 8개**
- 최근 서재 업데이트(읽기 상태/메모 포함): **최대 8개**
- 최근 검색어: **최대 10개 (최근 30일)**  

컨텍스트에는 다음 정보가 포함됩니다.
- 리뷰 스냅샷: `bookTitle`, `authors`, `rating`, `genre`, `summary`, `keywords`, `createdAt`
- 서재 스냅샷: `bookTitle`, `authors`, `status`, `rating`, `memo`, `updatedAt`
- 검색어: 정규화된 최근 검색어 리스트

즉, **트래킹 데이터는 검색 로그 + 리뷰/서재 활동**이 사용되며, 별도의 클릭/체류시간 같은 실시간 행동 데이터는 여기에는 직접 포함되지 않습니다.

추천 설명 생성은 **사용자 취향 태그(tasteTag)**와 **추천 점수 정보**만 프롬프트에 포함됩니다.
- `tasteTag`는 `users.taste_tag` 컬럼에서 가져옵니다.
- 점수 정보는 호출 시점에서 전달된 `scoreDetails`를 그대로 사용합니다.

### 4) LLM 프롬프트 및 응답 형식
**사용자 취향 분석 프롬프트**
- 사용자 ID, 닉네임, 최근 활동 JSON을 포함
- 다음 JSON 형식으로 응답 요구:
  - `persona_type`, `summary`, `keywords[]`, `recommendations[]`

**추천 설명 프롬프트**
- 사용자 닉네임, 취향 태그, 책 제목, 점수 정보를 포함
- 자연어 한두 문장 설명 생성

### 5) 캐시/DB 저장 정책
사용자 취향 분석 결과는 다음 정책으로 저장/재사용됩니다.
- 캐시 키: `user_analysis_{userId}_{yyyy-MM-dd}` (하루 1회)
- Redis Semantic Cache + DB 저장
- DB 저장 시 TTL: 24시간(만료 시간 기록)

### 6) 추천 도서 검증(실존 도서 체크)
LLM이 생성한 추천 도서는 외부 검색 API로 검증됩니다.
- 검색 결과가 없으면 제외
- 검색 1순위 결과로 제목/저자 정규화

### 7) AI 기능 활성화 조건
AI 기능은 설정값에 따라 전체가 on/off 됩니다.
- `ai.enrichment.enabled`가 true일 때만 활성화
- 비활성화 시 분석/설명은 빈 결과로 처리

### 8) 요약 (비개발자용 한 줄)
“최근 리뷰·서재 활동·검색어를 모아서 하루 한 번 AI가 취향을 요약하고, 추천 도서와 이유를 자연어로 알려주는 기능입니다.”

---

### 관련 코드 위치
- 사용자 취향 분석 API: `api/src/main/java/org/yyubin/api/ai/AIController.java`
- 취향 분석 오케스트레이션: `application/src/main/java/org/yyubin/application/recommendation/service/UserPreferenceAnalysisService.java`
- 컨텍스트 수집(리뷰/서재/검색어): `infrastructure/src/main/java/org/yyubin/infrastructure/recommendation/adapter/UserAnalysisContextAdapter.java`
- LLM 호출/프롬프트: `application/src/main/java/org/yyubin/application/recommendation/service/UserAnalysisLLMService.java`
- 캐시/DB 저장: `application/src/main/java/org/yyubin/application/recommendation/service/UserAnalysisCacheService.java`,
  `application/src/main/java/org/yyubin/application/recommendation/service/UserAnalysisPersistenceService.java`
- 추천 설명 생성: `application/src/main/java/org/yyubin/application/recommendation/usecase/GenerateRecommendationExplanationUseCase.java`
- AI 실행 게이트: `recommendation/src/main/java/org/yyubin/recommendation/service/AIEnrichmentService.java`
- LLM 어댑터: `infrastructure/src/main/java/org/yyubin/infrastructure/recommendation/adapter/OpenAIAdapter.java`
