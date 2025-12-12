package org.yyubin.recommendation.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 추천 후보
 * - 후보 생성 단계에서 만들어지는 중간 객체
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationCandidate {

    private Long bookId;

    // 후보 소스
    private CandidateSource source;

    // 초기 점수 (0.0 ~ 1.0)
    private Double initialScore;

    // 메타데이터
    private String reason;  // 추천 이유 (디버깅용)

    public enum CandidateSource {
        NEO4J_COLLABORATIVE,    // Neo4j 협업 필터링
        NEO4J_GENRE,            // Neo4j 장르 기반
        NEO4J_AUTHOR,           // Neo4j 저자 기반
        NEO4J_TOPIC,            // Neo4j 토픽 기반
        ELASTICSEARCH_SEMANTIC, // Elasticsearch 시맨틱 검색
        ELASTICSEARCH_MLT,      // Elasticsearch More Like This
        POPULARITY,             // 인기도 기반
        RECENT                  // 최근 출간
    }
}
