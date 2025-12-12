package org.yyubin.recommendation.candidate;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.search.document.ReviewDocument;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewElasticsearchCandidateGenerator {

    private final ReviewDocumentRepository reviewDocumentRepository;
    private final ReviewRecommendationProperties properties;

    /**
     * 피드 전용 후보 생성 (전체 인기/최근)
     */
    public List<ReviewRecommendationCandidate> generateFeedCandidates(Long userId, int limit) {
        int fetch = Math.max(limit * 2, limit);
        Pageable pageable = PageRequest.of(0, fetch);

        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();

        try {
            reviewDocumentRepository.findByOrderByLikeCountDesc(pageable)
                    .forEach(doc -> candidates.add(toCandidate(doc, ReviewRecommendationCandidate.CandidateSource.POPULARITY)));
        } catch (Exception e) {
            log.warn("Failed to load popular reviews for user {}", userId, e);
        }

        if (candidates.size() < limit) {
            // 부족하면 최신 리뷰로 보강
            Pageable recentPage = PageRequest.of(0, limit);
            try {
                reviewDocumentRepository.findAll(recentPage).forEach(doc ->
                        candidates.add(toCandidate(doc, ReviewRecommendationCandidate.CandidateSource.RECENT)));
            } catch (Exception e) {
                log.warn("Failed to load recent reviews for user {}", userId, e);
            }
        }

        return candidates.stream()
                .limit(properties.getMaxCandidates())
                .toList();
    }

    /**
     * 특정 도서 컨텍스트용 인기 리뷰 후보
     */
    public List<ReviewRecommendationCandidate> generateBookScopedCandidates(Long bookId, int limit) {
        Pageable pageable = PageRequest.of(0, limit * 2);
        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();

        try {
            reviewDocumentRepository.findPublicReviewsByBook(bookId, pageable)
                    .forEach(doc -> candidates.add(toCandidate(doc, ReviewRecommendationCandidate.CandidateSource.BOOK_POPULAR)));
        } catch (Exception e) {
            log.warn("Failed to load book scoped reviews for book {}", bookId, e);
        }

        return candidates.stream()
                .limit(properties.getMaxCandidates())
                .toList();
    }

    private ReviewRecommendationCandidate toCandidate(ReviewDocument doc, ReviewRecommendationCandidate.CandidateSource source) {
        double popularityScore = calculatePopularityScore(
                doc.getLikeCount(),
                doc.getCommentCount(),
                doc.getBookmarkCount(),
                doc.getViewCount()
        );

        return ReviewRecommendationCandidate.builder()
                .reviewId(parseLong(doc.getId()))
                .bookId(doc.getBookId())
                .source(source)
                .initialScore(popularityScore)
                .reason(buildReason(source, doc))
                .createdAt(doc.getCreatedAt())
                .build();
    }

    private double calculatePopularityScore(Integer likeCount, Integer commentCount, Integer bookmarkCount, Long viewCount) {
        int likes = likeCount != null ? likeCount : 0;
        int comments = commentCount != null ? commentCount : 0;
        int bookmarks = bookmarkCount != null ? bookmarkCount : 0;
        long views = viewCount != null ? viewCount : 0L;

        double weighted = (likes * 1.0) + (comments * 2.0) + (bookmarks * 1.5) + Math.log1p(views) * 0.5;
        return Math.min(1.0, Math.log10(weighted + 1) / 2.5);
    }

    private String buildReason(ReviewRecommendationCandidate.CandidateSource source, ReviewDocument doc) {
        return switch (source) {
            case BOOK_POPULAR -> "Popular review for book " + doc.getBookId();
            case POPULARITY -> "Trending review";
            case RECENT -> "Recent review";
            default -> source.name();
        };
    }

    private Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
