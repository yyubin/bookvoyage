package org.yyubin.recommendation.review.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewContentRepository extends ElasticsearchRepository<ReviewContentDocument, Long> {

    Page<ReviewContentDocument> findByHighlightsNormOrderByReviewIdDesc(String highlightNorm, Pageable pageable);
}
