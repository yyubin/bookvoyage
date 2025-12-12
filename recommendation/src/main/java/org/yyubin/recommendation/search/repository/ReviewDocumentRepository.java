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
