package org.yyubin.recommendation.review.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ReviewContentRepository extends ElasticsearchRepository<ReviewContentDocument, Long> {

    Page<ReviewContentDocument> findByHighlightsNormOrderByReviewIdDesc(String highlightNorm, Pageable pageable);

    Page<ReviewContentDocument> findByBookId(Long bookId, Pageable pageable);

    /**
     * 키워드(태그)로 검색
     * - Keyword 타입이므로 정확히 일치해야 함
     */
    Page<ReviewContentDocument> findByKeywordsContaining(String keyword, Pageable pageable);

    /**
     * 통합 검색: content, summary, highlights, keywords 모두 검색
     * - "실존"을 검색하면 "실존주의"도 매칭됨 (nori_analyzer)
     *
     * @param query 검색어
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    @Query("""
        {
          "bool": {
            "should": [
              {
                "match": {
                  "content": {
                    "query": "?0",
                    "boost": 1.0
                  }
                }
              },
              {
                "match": {
                  "summary": {
                    "query": "?0",
                    "boost": 1.5
                  }
                }
              },
              {
                "match": {
                  "highlights": {
                    "query": "?0",
                    "boost": 2.0
                  }
                }
              },
              {
                "wildcard": {
                  "keywords": {
                    "value": "*?0*",
                    "boost": 3.0
                  }
                }
              }
            ],
            "minimum_should_match": 1
          }
        }
        """)
    Page<ReviewContentDocument> searchByQuery(String query, Pageable pageable);
}
