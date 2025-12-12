package org.yyubin.recommendation.candidate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.search.document.BookDocument;
import org.yyubin.recommendation.search.repository.BookDocumentRepository;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<RecommendationCandidate> generateCandidates(Long userId, int limit) {
        log.debug("Generating Elasticsearch candidates for user: {}", userId);

        List<RecommendationCandidate> candidates = new ArrayList<>();

        // TODO: 실제로는 사용자의 최근 검색어, 조회 이력을 기반으로 쿼리 생성
        // 현재는 인기 도서로 대체
        candidates.addAll(generatePopularBooksCandidates(limit));

        log.debug("Generated {} Elasticsearch candidates for user {}", candidates.size(), userId);

        return candidates;
    }

    /**
     * 인기 도서 후보 생성
     */
    private List<RecommendationCandidate> generatePopularBooksCandidates(int limit) {
        List<BookDocument> popularBooks = bookDocumentRepository
                .findTop100ByOrderByViewCountDescWishlistCountDesc();

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
