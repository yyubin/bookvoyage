package org.yyubin.recommendation.candidate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yyubin.recommendation.config.RecommendationProperties;
import org.yyubin.recommendation.graph.node.BookNode;
import org.yyubin.recommendation.graph.repository.UserNodeRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Neo4j 기반 후보 생성기
 * - 그래프 관계를 활용한 협업 필터링
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class Neo4jCandidateGenerator implements CandidateGenerator {

    private final UserNodeRepository userNodeRepository;
    private final RecommendationProperties properties;

    @Override
    public List<RecommendationCandidate> generateCandidates(Long userId, int limit) {
        log.debug("Generating Neo4j candidates for user: {}", userId);

        Set<RecommendationCandidate> candidates = new HashSet<>();
        int perQueryLimit = limit / 4; // 4가지 쿼리에 균등 분배

        // 1. 협업 필터링 기반
        candidates.addAll(generateCollaborativeFilteringCandidates(userId, perQueryLimit));

        // 2. 장르 기반
        candidates.addAll(generateGenreBasedCandidates(userId, perQueryLimit));

        // 3. 저자 기반
        candidates.addAll(generateAuthorBasedCandidates(userId, perQueryLimit));

        // 4. 유사 도서 (k-hop 이웃)
        candidates.addAll(generateSimilarBooksCandidates(userId, perQueryLimit));

        log.debug("Generated {} Neo4j candidates for user {}", candidates.size(), userId);

        return new ArrayList<>(candidates);
    }

    /**
     * 협업 필터링: 비슷한 취향의 사용자들이 본 도서
     */
    private List<RecommendationCandidate> generateCollaborativeFilteringCandidates(Long userId, int limit) {
        List<Object[]> results = userNodeRepository.findBooksByCollaborativeFiltering(userId, limit);

        return results.stream()
                .map(row -> {
                    BookNode book = (BookNode) row[0];
                    Long similarUserCount = ((Number) row[1]).longValue();

                    double score = Math.min(1.0, similarUserCount / 10.0); // 10명 이상이면 1.0

                    return RecommendationCandidate.builder()
                            .bookId(book.getId())
                            .source(RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE)
                            .initialScore(score)
                            .reason("Similar users liked this (" + similarUserCount + " users)")
                            .build();
                })
                .toList();
    }

    /**
     * 장르 기반: 사용자가 선호하는 장르의 도서
     */
    private List<RecommendationCandidate> generateGenreBasedCandidates(Long userId, int limit) {
        List<Object[]> results = userNodeRepository.findBooksByPreferredGenres(userId, limit);

        return results.stream()
                .map(row -> {
                    BookNode book = (BookNode) row[0];
                    Long genreOverlap = ((Number) row[1]).longValue();

                    double score = Math.min(1.0, genreOverlap / 3.0); // 3개 이상 겹치면 1.0

                    return RecommendationCandidate.builder()
                            .bookId(book.getId())
                            .source(RecommendationCandidate.CandidateSource.NEO4J_GENRE)
                            .initialScore(score)
                            .reason("Genre overlap: " + genreOverlap)
                            .build();
                })
                .toList();
    }

    /**
     * 저자 기반: 사용자가 선호하는 저자의 다른 도서
     */
    private List<RecommendationCandidate> generateAuthorBasedCandidates(Long userId, int limit) {
        List<Object[]> results = userNodeRepository.findBooksByPreferredAuthors(userId, limit);

        return results.stream()
                .map(row -> {
                    BookNode book = (BookNode) row[0];
                    Long authorOverlap = ((Number) row[1]).longValue();

                    double score = Math.min(1.0, authorOverlap / 2.0); // 2명 이상 겹치면 1.0

                    return RecommendationCandidate.builder()
                            .bookId(book.getId())
                            .source(RecommendationCandidate.CandidateSource.NEO4J_AUTHOR)
                            .initialScore(score)
                            .reason("Author overlap: " + authorOverlap)
                            .build();
                })
                .toList();
    }

    /**
     * 유사 도서: k-hop 이웃 탐색
     */
    private List<RecommendationCandidate> generateSimilarBooksCandidates(Long userId, int limit) {
        List<Object[]> results = userNodeRepository.findSimilarBooks(userId, limit);

        return results.stream()
                .map(row -> {
                    BookNode book = (BookNode) row[0];
                    Long pathCount = ((Number) row[1]).longValue();

                    double score = Math.min(1.0, pathCount / 5.0); // 5개 이상 경로면 1.0

                    return RecommendationCandidate.builder()
                            .bookId(book.getId())
                            .source(RecommendationCandidate.CandidateSource.NEO4J_TOPIC)
                            .initialScore(score)
                            .reason("Similar books via graph: " + pathCount + " paths")
                            .build();
                })
                .toList();
    }

    @Override
    public RecommendationCandidate.CandidateSource getSourceType() {
        return RecommendationCandidate.CandidateSource.NEO4J_COLLABORATIVE;
    }
}
