package org.yyubin.recommendation.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.search.document.ReviewDocument;

import java.util.List;

/**
 * ReviewDocument Repository
 * - Elasticsearch 기반 리뷰 검색
 */
@Repository
public interface ReviewDocumentRepository extends ElasticsearchRepository<ReviewDocument, String> {

    /**
     * 특정 도서의 리뷰 검색
     */
    Page<ReviewDocument> findByBookId(Long bookId, Pageable pageable);

    /**
     * 특정 사용자의 리뷰 검색
     */
    Page<ReviewDocument> findByUserId(Long userId, Pageable pageable);

    /**
     * 리뷰 내용 검색
     */
    Page<ReviewDocument> findByContentContaining(String keyword, Pageable pageable);

    /**
     * 높은 평점 리뷰만 검색
     */
    List<ReviewDocument> findByRatingGreaterThanEqual(Float minRating, Pageable pageable);

    /**
     * 인기 리뷰 (좋아요 많은 순)
     */
    Page<ReviewDocument> findByOrderByLikeCountDesc(Pageable pageable);

    /**
     * 관심사 기반 리뷰 검색 (Multi-match)
     */
    @Query("""
            {
              "bool": {
                "must": [
                  {
                    "multi_match": {
                      "query": "?0",
                      "fields": [
                        "title^2",
                        "summary^1.5",
                        "content",
                        "highlights",
                        "searchableText",
                        "keywords^2",
                        "bookTitle^1.5",
                        "genre^1.2"
                      ],
                      "type": "best_fields",
                      "fuzziness": "AUTO"
                    }
                  }
                ],
                "filter": [
                  { "term": { "visibility": "PUBLIC" } }
                ],
                "must_not": [
                  { "term": { "userId": "?1" } }
                ]
              }
            }
            """)
    Page<ReviewDocument> searchByMultiMatch(String query, Long userId, Pageable pageable);

    /**
     * 관심사 기반 리뷰 검색 (More Like This)
     */
    @Query("""
            {
              "bool": {
                "must": [
                  {
                    "more_like_this": {
                      "fields": [
                        "title",
                        "summary",
                        "content",
                        "highlights",
                        "searchableText",
                        "keywords",
                        "bookTitle",
                        "genre"
                      ],
                      "like": [
                        {
                          "_index": "review_content",
                          "_id": "?0"
                        }
                      ],
                      "min_term_freq": 1,
                      "min_doc_freq": 1,
                      "max_query_terms": 25
                    }
                  }
                ],
                "filter": [
                  { "term": { "visibility": "PUBLIC" } }
                ],
                "must_not": [
                  { "term": { "userId": "?1" } }
                ]
              }
            }
            """)
    Page<ReviewDocument> findSimilarReviews(String reviewId, Long userId, Pageable pageable);

    /**
     * 특정 도서의 인기 리뷰
     */
    @Query("""
            {
              "bool": {
                "must": [
                  { "term": { "bookId": "?0" } },
                  { "term": { "visibility": "PUBLIC" } }
                ]
              }
            }
            """)
    Page<ReviewDocument> findPublicReviewsByBook(Long bookId, Pageable pageable);
}
