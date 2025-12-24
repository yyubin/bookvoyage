package org.yyubin.recommendation.review;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yyubin.domain.review.HighlightNormalizer;
import org.yyubin.recommendation.review.graph.HighlightNode;
import org.yyubin.recommendation.review.graph.ReviewNode;
import org.yyubin.recommendation.review.graph.ReviewNodeRepository;
import org.yyubin.recommendation.review.search.ReviewContentDocument;
import org.yyubin.recommendation.review.search.ReviewContentRepository;

@Service
@RequiredArgsConstructor
public class HighlightReviewRecommendationService {

    private final ReviewContentRepository reviewContentRepository;
    private final ReviewNodeRepository reviewNodeRepository;
    private final HighlightNormalizer highlightNormalizer;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ReviewHighlightRecommendationProperties highlightProperties;

    @Transactional
    public void indexReviewContent(ReviewContentDocument document) {
        reviewContentRepository.save(document);
    }

    @Transactional
    public void ingest(RecommendationIngestCommand command) {
        ReviewContentDocument document = ReviewContentDocument.builder()
                .reviewId(command.reviewId())
                .userId(command.userId())
                .bookId(command.bookId())
                .summary(command.summary())
                .content(command.content())
                .highlights(command.highlights())
                .highlightsNorm(command.highlightsNorm())
                .keywords(command.keywords())
                .genre(command.genre())
                .createdAt(command.createdAt())
                .rating(command.rating())
                .build();
        reviewContentRepository.save(document);
        upsertReviewHighlights(command.reviewId(), command.userId(), command.bookId(), command.highlights());
    }

    @Transactional
    public void upsertReviewHighlights(Long reviewId, Long userId, Long bookId, List<String> rawHighlights) {
        Set<HighlightNode> highlights = new LinkedHashSet<>();
        if (rawHighlights != null) {
            for (String raw : rawHighlights) {
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                String normalized = highlightNormalizer.normalize(raw);
                highlights.add(new HighlightNode(normalized, raw));
            }
        }
        ReviewNode node = new ReviewNode(reviewId, userId, bookId, highlights);
        reviewNodeRepository.save(node);
    }

    @Transactional(readOnly = true)
    public HighlightRecommendationResult recommendByHighlight(String highlight, Long cursor, int size) {
        if (highlight == null || highlight.isBlank()) {
            throw new IllegalArgumentException("Highlight must not be empty");
        }
        String normalized = highlightNormalizer.normalize(highlight);

        int fetchSize = Math.min(highlightProperties.getMaxCandidates(), size + 1);
        List<Long> graphIds = reviewNodeRepository.findReviewIdsByHighlight(normalized, cursor, fetchSize);
        Map<Long, Double> graphScores = scoreGraph(graphIds, cursor);

        Map<Long, Double> esScores = scoreSearch(normalized, highlight, cursor, fetchSize);

        Set<Long> merged = new LinkedHashSet<>();
        merged.addAll(graphScores.keySet());
        merged.addAll(esScores.keySet());

        List<ScoredReview> scored = new ArrayList<>();
        double maxEs = maxScore(esScores);
        double maxGraph = maxScore(graphScores);

        for (Long reviewId : merged) {
            double esScore = esScores.getOrDefault(reviewId, 0.0);
            double graphScore = graphScores.getOrDefault(reviewId, 0.0);
            double normalizedEs = maxEs > 0 ? esScore / maxEs : 0.0;
            double normalizedGraph = maxGraph > 0 ? graphScore / maxGraph : 0.0;
            double score = highlightProperties.getEsWeight() * normalizedEs
                    + highlightProperties.getGraphWeight() * normalizedGraph;
            scored.add(new ScoredReview(reviewId, score));
        }

        scored.sort((a, b) -> {
            int scoreCompare = Double.compare(b.score(), a.score());
            if (scoreCompare != 0) {
                return scoreCompare;
            }
            return Long.compare(b.reviewId(), a.reviewId());
        });

        List<Long> ordered = scored.stream().map(ScoredReview::reviewId).toList();
        Long nextCursor = ordered.size() > size ? ordered.get(size) : null;
        List<Long> page = ordered.size() > size ? ordered.subList(0, size) : ordered;

        return new HighlightRecommendationResult(page, nextCursor);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        reviewContentRepository.deleteById(reviewId);
        reviewNodeRepository.deleteById(reviewId);
    }

    private Map<Long, Double> scoreSearch(String normalized, String highlight, Long cursor, int limit) {
        Criteria criteria = new Criteria("highlightsNorm").is(normalized)
                .and(
                        new Criteria("highlights").matches(highlight)
                                .or(new Criteria("summary").matches(highlight))
                );
        CriteriaQuery query = new CriteriaQuery(criteria);
        query.setPageable(PageRequest.of(0, limit));

        SearchHits<ReviewContentDocument> hits = elasticsearchOperations.search(query, ReviewContentDocument.class);
        java.util.Map<Long, Double> scores = new java.util.HashMap<>();

        for (SearchHit<ReviewContentDocument> hit : hits) {
            Long reviewId = hit.getContent().getReviewId();
            if (cursor == null || reviewId < cursor) {
                scores.put(reviewId, (double) hit.getScore());
            }
        }
        return scores;
    }

    private Map<Long, Double> scoreGraph(List<Long> reviewIds, Long cursor) {
        java.util.Map<Long, Double> scores = new java.util.HashMap<>();
        for (Long id : reviewIds) {
            if (cursor == null || id < cursor) {
                scores.put(id, 1.0);
            }
        }
        return scores;
    }

    private double maxScore(Map<Long, Double> scores) {
        return scores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
    }

    private record ScoredReview(Long reviewId, double score) {}
}
