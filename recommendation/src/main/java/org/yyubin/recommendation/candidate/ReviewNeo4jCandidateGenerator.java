package org.yyubin.recommendation.candidate;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;
import org.yyubin.recommendation.review.graph.ReviewNode;
import org.yyubin.recommendation.review.graph.ReviewNodeRepository;

/**
 * Neo4j 기반 리뷰 후보 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewNeo4jCandidateGenerator {

    private final UserNodeRepository userNodeRepository;
    private final ReviewNodeRepository reviewNodeRepository;
    private final ReviewRecommendationProperties properties;

    public List<ReviewRecommendationCandidate> generateFeedCandidates(Long userId, int limit) {
        if (userId == null || limit <= 0) {
            return List.of();
        }

        int similarLimit = Math.max(0, (int) Math.round(limit * properties.getSearch().getGraphSimilarRatio()));
        int bookLimit = Math.max(0, limit - similarLimit);

        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        candidates.addAll(generateBySimilarUsers(userId, similarLimit));
        candidates.addAll(generateByBookAffinity(userId, bookLimit));

        return candidates;
    }

    private List<ReviewRecommendationCandidate> generateBySimilarUsers(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        try {
            List<Object[]> rows = userNodeRepository.findReviewIdsBySimilarUsers(userId, limit);
            for (Object[] row : rows) {
                Long reviewId = asLong(row, 0);
                Long bookId = asLong(row, 1);
                Long score = asLong(row, 2);
                if (reviewId != null) {
                    candidates.add(ReviewRecommendationCandidate.builder()
                            .reviewId(reviewId)
                            .bookId(bookId)
                            .source(ReviewRecommendationCandidate.CandidateSource.GRAPH_SIMILAR_USER)
                            .initialScore(normalizeScore(score))
                            .reason("Similar readers liked this review")
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load graph review candidates by similar users for user {}", userId, e);
        }
        return candidates;
    }

    private List<ReviewRecommendationCandidate> generateByBookAffinity(Long userId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        int bookLimit = Math.max(1, properties.getSearch().getGraphBookSeedLimit());
        List<Long> bookIds = userNodeRepository.findInteractedBookIds(userId, bookLimit);
        if (bookIds.isEmpty()) {
            return List.of();
        }

        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        try {
            List<ReviewNode> reviews = reviewNodeRepository.findReviewsByBookIds(bookIds, userId, limit);
            for (ReviewNode review : reviews) {
                candidates.add(ReviewRecommendationCandidate.builder()
                        .reviewId(review.getReviewId())
                        .bookId(review.getBookId())
                        .source(ReviewRecommendationCandidate.CandidateSource.GRAPH_BOOK_AFFINITY)
                        .initialScore(0.6)
                        .reason("Reviews from books you engaged with")
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to load graph review candidates by book affinity for user {}", userId, e);
        }

        return candidates;
    }

    private double normalizeScore(Long score) {
        if (score == null) {
            return 0.5;
        }
        return Math.min(1.0, score / 5.0);
    }

    private Long asLong(Object[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return null;
        }
        Object value = row[index];
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
