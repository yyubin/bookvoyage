package org.yyubin.recommendation.candidate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.yyubin.application.recommendation.port.out.UserAnalysisContextPort;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Elasticsearch 기반 후보 생성기
 * - 텍스트 기반 콘텐츠 필터링
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticsearchCandidateGenerator implements CandidateGenerator {

    private final BookDocumentRepository bookDocumentRepository;
    private final RecommendationProperties properties;
    private final UserAnalysisContextPort userAnalysisContextPort;

    private static final int DEFAULT_SEED_LIMIT = 3;

    @Override
    public List<RecommendationCandidate> generateCandidates(Long userId, int limit) {
        log.debug("Generating Elasticsearch candidates for user: {}", userId);

        List<RecommendationCandidate> candidates = new ArrayList<>();

        if (limit <= 0) {
            return candidates;
        }

        List<BookDocument> popularBooks = bookDocumentRepository
                .findTop100ByOrderByViewCountDescWishlistCountDesc();
        if (popularBooks.isEmpty()) {
            return candidates;
        }

        UserAnalysisContextPort.UserAnalysisContext context = loadUserContext(userId);
        List<Long> mltSeeds = resolveMltSeeds(context);
        List<String> semanticQueries = resolveSemanticQueries(context);

        int popularityLimit = Math.max(1, limit / 2);
        int mltLimit = Math.max(0, limit / 4);
        int semanticLimit = Math.max(0, limit - popularityLimit - mltLimit);

        if (mltSeeds.isEmpty()) {
            popularityLimit += mltLimit;
            mltLimit = 0;
        }
        if (semanticQueries.isEmpty()) {
            popularityLimit += semanticLimit;
            semanticLimit = 0;
        }

        candidates.addAll(generatePopularBooksCandidates(popularBooks, popularityLimit));
        candidates.addAll(generateMltCandidates(mltSeeds, mltLimit));
        candidates.addAll(generateSemanticCandidates(semanticQueries, semanticLimit));

        log.debug("Generated {} Elasticsearch candidates for user {}", candidates.size(), userId);

        return candidates;
    }

    /**
     * 인기 도서 후보 생성
     */
    private List<RecommendationCandidate> generatePopularBooksCandidates(List<BookDocument> popularBooks, int limit) {
        return popularBooks.stream()
                .limit(limit)
                .map(book -> {
                    // 정규화된 인기 점수 계산
                    double popularityScore = calculatePopularityScore(
                            book.getViewCount(),
                            book.getWishlistCount(),
                            book.getReviewCount()
                    );

                    return RecommendationCandidate.builder()
                            .bookId(Long.parseLong(book.getId()))
                            .source(RecommendationCandidate.CandidateSource.POPULARITY)
                            .initialScore(popularityScore)
                            .reason("Popular book (views: " + book.getViewCount() + ")")
                            .build();
                })
                .toList();
    }

    private List<RecommendationCandidate> generateMltCandidates(List<Long> seedBookIds, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        int seeds = Math.min(DEFAULT_SEED_LIMIT, seedBookIds.size());
        int perSeedLimit = Math.max(1, limit / seeds);

        List<RecommendationCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < seeds; i++) {
            Long seedBookId = seedBookIds.get(i);
            candidates.addAll(generateMoreLikeThisCandidates(seedBookId, perSeedLimit));
        }

        return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
    }

    private List<RecommendationCandidate> generateSemanticCandidates(List<String> queries, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        int seeds = Math.min(DEFAULT_SEED_LIMIT, queries.size());
        int perSeedLimit = Math.max(1, limit / seeds);

        List<RecommendationCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < seeds; i++) {
            String query = queries.get(i);
            candidates.addAll(generateSemanticSearchCandidates(query, perSeedLimit));
        }

        return candidates.size() > limit ? candidates.subList(0, limit) : candidates;
    }

    private UserAnalysisContextPort.UserAnalysisContext loadUserContext(Long userId) {
        if (userId == null) {
            return null;
        }
        RecommendationProperties.SearchConfig searchConfig = properties.getSearch();
        int reviewLimit = searchConfig.getContextReviewLimit();
        int libraryLimit = searchConfig.getContextLibraryLimit();
        int searchLimit = searchConfig.getContextSearchLimit();
        int searchDays = searchConfig.getContextSearchDays();
        LocalDateTime since = LocalDateTime.now().minusDays(searchDays);

        try {
            return userAnalysisContextPort.loadContext(userId, reviewLimit, libraryLimit, searchLimit, since);
        } catch (Exception e) {
            log.warn("Failed to load user context for Elasticsearch candidates userId={}", userId, e);
            return null;
        }
    }

    private List<Long> resolveMltSeeds(UserAnalysisContextPort.UserAnalysisContext context) {
        Set<Long> seeds = new LinkedHashSet<>();
        if (context == null) {
            return List.of();
        }

        for (UserAnalysisContextPort.ReviewSnapshot review : context.recentReviews()) {
            if (review.bookId() != null) {
                seeds.add(review.bookId());
            }
        }
        for (UserAnalysisContextPort.UserBookSnapshot book : context.recentLibraryUpdates()) {
            if (book.bookId() != null) {
                seeds.add(book.bookId());
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

    /**
     * More Like This 기반 후보 생성
     *
     * @param bookId 기준 도서 ID
     * @param limit 결과 수
     */
    public List<RecommendationCandidate> generateMoreLikeThisCandidates(Long bookId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        return bookDocumentRepository.findSimilarBooks(String.valueOf(bookId), pageable)
                .stream()
                .map(book -> RecommendationCandidate.builder()
                        .bookId(Long.parseLong(book.getId()))
                        .source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_MLT)
                        .initialScore(0.7) // MLT는 기본 0.7 점수
                        .reason("Similar to book " + bookId)
                        .build())
                .toList();
    }

    /**
     * 시맨틱 검색 기반 후보 생성
     *
     * @param query 검색어
     * @param limit 결과 수
     */
    public List<RecommendationCandidate> generateSemanticSearchCandidates(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);

        return bookDocumentRepository.searchByMultiMatch(query, pageable)
                .stream()
                .map(book -> RecommendationCandidate.builder()
                        .bookId(Long.parseLong(book.getId()))
                        .source(RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC)
                        .initialScore(0.6) // 검색 기반은 0.6 기본 점수
                        .reason("Matched query: " + query)
                        .build())
                .toList();
    }

    /**
     * 인기도 점수 계산
     */
    private double calculatePopularityScore(Integer viewCount, Integer wishlistCount, Integer reviewCount) {
        if (viewCount == null) viewCount = 0;
        if (wishlistCount == null) wishlistCount = 0;
        if (reviewCount == null) reviewCount = 0;

        // 가중 평균
        double weighted = (viewCount * 1.0) + (wishlistCount * 5.0) + (reviewCount * 3.0);

        // 로그 스케일로 정규화 (0.0 ~ 1.0)
        return Math.min(1.0, Math.log10(weighted + 1) / 4.0);
    }

    @Override
    public RecommendationCandidate.CandidateSource getSourceType() {
        return RecommendationCandidate.CandidateSource.ELASTICSEARCH_SEMANTIC;
    }
}
