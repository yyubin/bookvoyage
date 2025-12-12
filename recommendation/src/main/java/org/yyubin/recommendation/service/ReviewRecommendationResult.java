package org.yyubin.recommendation.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRecommendationResult {
    private Long reviewId;
    private Long bookId;
    private Double score;
    private Integer rank;
    private String source;
    private String reason;
    private java.time.LocalDateTime createdAt;
}
