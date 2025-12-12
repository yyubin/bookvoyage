package org.yyubin.recommendation.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResult {

    private Long bookId;
    private Double score;
    private Integer rank;

    // 메타데이터
    private String source;
    private String reason;
}
