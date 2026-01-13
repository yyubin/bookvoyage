package org.yyubin.recommendation.candidate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.recommendation.config.ReviewRecommendationProperties;
import org.yyubin.recommendation.search.document.ReviewDocument;
import org.yyubin.recommendation.search.repository.ReviewDocumentRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewElasticsearchCandidateGenerator {

    private final ReviewDocumentRepository reviewDocumentRepository;
    private final ReviewRecommendationProperties properties;
    private final UserAnalysisContextPort userAnalysisContextPort;

    private static final int DEFAULT_SEED_LIMIT = 3;

    /**
     * 피드 전용 후보 생성 (관심사 기반 + 인기/최근)
     */
    public List<ReviewRecommendationCandidate> generateFeedCandidates(Long userId, int limit) {
        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        int totalLimit = Math.max(limit, 1);
        int fetch = Math.max(totalLimit * 2, totalLimit);

        int interestLimit = resolveInterestLimit(totalLimit);
        if (userId != null && interestLimit > 0) {
            candidates.addAll(generateInterestCandidates(userId, interestLimit));
        }

        try {
            Pageable pageable = PageRequest.of(0, fetch);
            reviewDocumentRepository.findByOrderByLikeCountDesc(pageable)
                    .forEach(doc -> candidates.add(toCandidate(doc, ReviewRecommendationCandidate.CandidateSource.POPULARITY)));
        } catch (Exception e) {
            log.warn("Failed to load popular reviews for user {}", userId, e);
        }

        if (candidates.size() < totalLimit) {
            // 부족하면 최신 리뷰로 보강
            Pageable recentPage = PageRequest.of(0, totalLimit);
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

    private List<ReviewRecommendationCandidate> generateInterestCandidates(Long userId, int limit) {
        UserAnalysisContextPort.UserAnalysisContext context = loadUserContext(userId);
        if (context == null || limit <= 0) {
            return List.of();
        }

        List<Long> mltSeeds = resolveMltSeeds(context);
        List<String> semanticQueries = resolveSemanticQueries(context);

        int mltLimit = Math.max(0, (int) Math.round(limit * properties.getSearch().getFeedMltRatio()));
        int semanticLimit = Math.max(0, (int) Math.round(limit * properties.getSearch().getFeedSemanticRatio()));
        int remaining = Math.max(0, limit - mltLimit - semanticLimit);
        semanticLimit += remaining;

        if (mltSeeds.isEmpty()) {
            semanticLimit += mltLimit;
            mltLimit = 0;
        }
        if (semanticQueries.isEmpty()) {
            mltLimit += semanticLimit;
            semanticLimit = 0;
        }

        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        candidates.addAll(generateMltCandidates(userId, mltSeeds, mltLimit));
        candidates.addAll(generateSemanticCandidates(userId, semanticQueries, semanticLimit));
        return candidates;
    }

    private int resolveInterestLimit(int totalLimit) {
        double ratio = properties.getSearch().getFeedInterestRatio();
        int interestLimit = (int) Math.round(totalLimit * ratio);
        return Math.max(0, Math.min(totalLimit, interestLimit));
    }

    private List<ReviewRecommendationCandidate> generateMltCandidates(Long userId, List<Long> seedReviewIds, int limit) {
        if (limit <= 0 || seedReviewIds.isEmpty()) {
            return List.of();
        }
        int seedLimit = Math.min(properties.getSearch().getSeedLimit(), seedReviewIds.size());
        seedLimit = Math.min(seedLimit, DEFAULT_SEED_LIMIT);
        int perSeedLimit = Math.max(1, limit / seedLimit);

        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < seedLimit; i++) {
            Long seedReviewId = seedReviewIds.get(i);
            Pageable pageable = PageRequest.of(0, perSeedLimit);
            reviewDocumentRepository.findSimilarReviews(String.valueOf(seedReviewId), userId, pageable)
                    .forEach(doc -> candidates.add(
                            toCandidate(doc, ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW,
                                    "Similar to review " + seedReviewId)
                    ));
        }
        return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
    }

    private List<ReviewRecommendationCandidate> generateSemanticCandidates(Long userId, List<String> queries, int limit) {
        if (limit <= 0 || queries.isEmpty()) {
            return List.of();
        }
        int seedLimit = Math.min(properties.getSearch().getSeedLimit(), queries.size());
        seedLimit = Math.min(seedLimit, DEFAULT_SEED_LIMIT);
        int perSeedLimit = Math.max(1, limit / seedLimit);

        List<ReviewRecommendationCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < seedLimit; i++) {
            String query = queries.get(i);
            Pageable pageable = PageRequest.of(0, perSeedLimit);
            reviewDocumentRepository.searchByMultiMatch(query, userId, pageable)
                    .forEach(doc -> candidates.add(
                            toCandidate(doc, ReviewRecommendationCandidate.CandidateSource.SIMILAR_REVIEW,
                                    "Matched interest: " + query)
                    ));
        }
        return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
    }

    private UserAnalysisContextPort.UserAnalysisContext loadUserContext(Long userId) {
        if (userId == null) {
            return null;
        }
        ReviewRecommendationProperties.Search search = properties.getSearch();
        int reviewLimit = search.getContextReviewLimit();
        int libraryLimit = search.getContextLibraryLimit();
        int searchLimit = search.getContextSearchLimit();
        int searchDays = search.getContextSearchDays();
        LocalDateTime since = LocalDateTime.now().minusDays(searchDays);

        try {
            return userAnalysisContextPort.loadContext(userId, reviewLimit, libraryLimit, searchLimit, since);
        } catch (Exception e) {
            log.warn("Failed to load user context for review candidates userId={}", userId, e);
            return null;
        }
    }

    private List<Long> resolveMltSeeds(UserAnalysisContextPort.UserAnalysisContext context) {
        Set<Long> seeds = new LinkedHashSet<>();
        if (context == null) {
            return List.of();
        }

        for (UserAnalysisContextPort.ReviewSnapshot review : context.recentReviews()) {
            if (review.reviewId() != null) {
                seeds.add(review.reviewId());
            }
        }

        return new ArrayList<>(seeds);
    }

    private List<String> resolveSemanticQueries(UserAnalysisContextPort.UserAnalysisContext context) {
        Set<String> queries = new LinkedHashSet<>();
        if (context == null) {
            return List.of();
        }

        for (String query : context.recentSearchQueries()) {
            if (query != null && !query.isBlank()) {
                queries.add(query);
            }
        }
        for (UserAnalysisContextPort.ReviewSnapshot review : context.recentReviews()) {
            String query = titleAuthorQuery(review.bookTitle(), review.bookAuthors());
            if (query != null) {
                queries.add(query);
            }
        }
        for (UserAnalysisContextPort.UserBookSnapshot book : context.recentLibraryUpdates()) {
            String query = titleAuthorQuery(book.bookTitle(), book.bookAuthors());
            if (query != null) {
                queries.add(query);
            }
        }

        return new ArrayList<>(queries);
    }

    private String titleAuthorQuery(String title, List<String> authors) {
        if (title == null || title.isBlank()) {
            return null;
        }
        if (authors == null || authors.isEmpty() || authors.get(0).isBlank()) {
            return title;
        }
        return title + " " + authors.get(0);
    }

    private ReviewRecommendationCandidate toCandidate(ReviewDocument doc, ReviewRecommendationCandidate.CandidateSource source) {
        return toCandidate(doc, source, buildReason(source, doc));
    }

    private ReviewRecommendationCandidate toCandidate(
            ReviewDocument doc,
            ReviewRecommendationCandidate.CandidateSource source,
            String reason
    ) {
        double popularityScore = calculatePopularityScore(
                doc.getLikeCount(),
                doc.getCommentCount(),
                doc.getBookmarkCount(),
                doc.getViewCount()
        );

        Long reviewId = doc.getReviewId() != null ? doc.getReviewId() : parseLong(doc.getId());

        return ReviewRecommendationCandidate.builder()
                .reviewId(reviewId)
                .bookId(doc.getBookId())
                .source(source)
                .initialScore(popularityScore)
                .reason(reason)
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
            case SIMILAR_REVIEW -> "Similar to your interests";
            case FOLLOWED_USER -> "From a followed user";
            default -> source.name();
        };
    }

    private Long parseLong(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
