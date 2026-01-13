package org.yyubin.recommendation.candidate;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 리뷰 추천 후보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRecommendationCandidate {

    private Long reviewId;
    private Long bookId;
    private CandidateSource source;
    private Double initialScore;
    private String reason;
    private LocalDateTime createdAt;

    public enum CandidateSource {
        POPULARITY,        // 인기 기반
        BOOK_POPULAR,      // 특정 도서 내 인기 리뷰
        SIMILAR_REVIEW,    // 비슷한 리뷰 기반
        FOLLOWED_USER,     // 팔로우 유저/사회적 신호
        RECENT,            // 최신성 기반
        GRAPH_SIMILAR_USER, // 유사 사용자 기반
        GRAPH_BOOK_AFFINITY // 관심 도서 기반
    }
}
