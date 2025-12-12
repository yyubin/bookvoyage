package org.yyubin.recommendation.graph.repository;

import java.util.List;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.yyubin.recommendation.graph.node.BookNode;

/**
 * BookNode Repository
 */
@Repository
public interface BookNodeRepository extends Neo4jRepository<BookNode, Long> {

    /**
     * 특정 도서와 유사한 도서 찾기 (장르/토픽 기반)
     */
    @Query("""
            MATCH (b1:Book {id: $bookId})-[:BELONGS_TO_GENRE|HAS_TOPIC]->(tag)
            MATCH (b2:Book)-[:BELONGS_TO_GENRE|HAS_TOPIC]->(tag)
            WHERE b1 <> b2
            RETURN b2, COUNT(DISTINCT tag) as tagOverlap
            ORDER BY tagOverlap DESC
            LIMIT $limit
            """)
    List<Object[]> findSimilarBooks(@Param("bookId") Long bookId, @Param("limit") int limit);

    /**
     * 특정 저자의 다른 도서들 찾기
     */
    @Query("""
            MATCH (b1:Book {id: $bookId})-[:WRITTEN_BY]->(a:Author)<-[:WRITTEN_BY]-(b2:Book)
            WHERE b1 <> b2
            RETURN b2
            ORDER BY b2.viewCount DESC
            LIMIT $limit
            """)
    List<BookNode> findBooksByAuthor(@Param("bookId") Long bookId, @Param("limit") int limit);

    /**
     * 인기 도서 조회 (전체)
     */
    @Query("""
            MATCH (b:Book)
            RETURN b
            ORDER BY b.viewCount DESC, b.wishlistCount DESC
            LIMIT $limit
            """)
    List<BookNode> findPopularBooks(@Param("limit") int limit);

    /**
     * 특정 장르의 인기 도서
     */
    @Query("""
            MATCH (b:Book)-[:BELONGS_TO_GENRE]->(g:Genre {name: $genreName})
            RETURN b
            ORDER BY b.viewCount DESC
            LIMIT $limit
            """)
    List<BookNode> findPopularBooksByGenre(@Param("genreName") String genreName, @Param("limit") int limit);
}
