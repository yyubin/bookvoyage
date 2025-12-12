package org.yyubin.recommendation.search.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.search.document.BookDocument;

import java.util.List;

/**
 * BookDocument Repository
 * - Elasticsearch 기반 도서 검색
 */
@Repository
public interface BookDocumentRepository extends ElasticsearchRepository<BookDocument, String> {

    /**
     * 명시적 bulk 저장 헬퍼
     */
    default <S extends BookDocument> java.util.List<S> bulkSaveAll(Iterable<S> documents) {
        java.util.List<S> saved = new java.util.ArrayList<>();
        this.saveAll(documents).forEach(saved::add);
        return saved;
    }

    /**
     * 제목으로 검색
     */
    Page<BookDocument> findByTitleContaining(String title, Pageable pageable);

    /**
     * 저자로 검색
     */
    Page<BookDocument> findByAuthorsContaining(String author, Pageable pageable);

    /**
     * 장르로 검색
     */
    Page<BookDocument> findByGenresContaining(String genre, Pageable pageable);

    /**
     * 토픽으로 검색
     */
    Page<BookDocument> findByTopicsContaining(String topic, Pageable pageable);

    /**
     * 복합 텍스트 검색 (제목 + 설명 + 저자)
     */
    Page<BookDocument> findBySearchableTextContaining(String searchText, Pageable pageable);

    /**
     * Multi-match 쿼리 (제목, 설명, 저자 동시 검색)
     */
    @Query("""
            {
              "multi_match": {
                "query": "?0",
                "fields": ["title^3", "description", "searchableText"],
                "type": "best_fields",
                "fuzziness": "AUTO"
              }
            }
            """)
    Page<BookDocument> searchByMultiMatch(String query, Pageable pageable);

    /**
     * More Like This 쿼리 (유사 도서 검색)
     */
    @Query("""
            {
              "more_like_this": {
                "fields": ["title", "description", "genres", "topics"],
                "like": [
                  {
                    "_index": "books",
                    "_id": "?0"
                  }
                ],
                "min_term_freq": 1,
                "min_doc_freq": 1
              }
            }
            """)
    Page<BookDocument> findSimilarBooks(String bookId, Pageable pageable);

    /**
     * 인기도 기준 정렬 검색
     */
    List<BookDocument> findTop100ByOrderByViewCountDescWishlistCountDesc();
}
